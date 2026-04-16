package com.goblinbank.market;

import com.goblinbank.GoblinConstants;
import com.goblinbank.account.HouseAccount;
import com.goblinbank.account.HouseAccountRepository;
import com.goblinbank.ledger.LedgerEntry;
import com.goblinbank.ledger.LedgerEntryRepository;
import com.goblinbank.ledger.LedgerEntryType;
import com.goblinbank.shareprice.SharePriceService;
import com.goblinbank.stock.StockPriceService;
import com.goblinbank.stock.StockType;
import com.goblinbank.stock.TradableStock;
import com.goblinbank.stock.TradableStockRepository;
import com.goblinbank.ticker.TickerBaselineService;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.Instant;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InvestmentService {

  private static final MathContext MC = new MathContext(24, RoundingMode.HALF_UP);

  private final HouseAccountRepository accountRepo;
  private final InvestmentPositionRepository positionRepo;
  private final LedgerEntryRepository ledgerRepo;
  private final SharePriceService sharePriceService;
  private final TradableStockRepository stockRepo;
  private final StockPriceService stockPriceService;
  private final TickerBaselineService tickerBaselineService;

  public InvestmentService(
      HouseAccountRepository accountRepo,
      InvestmentPositionRepository positionRepo,
      LedgerEntryRepository ledgerRepo,
      SharePriceService sharePriceService,
      TradableStockRepository stockRepo,
      StockPriceService stockPriceService,
      TickerBaselineService tickerBaselineService) {
    this.accountRepo = accountRepo;
    this.positionRepo = positionRepo;
    this.ledgerRepo = ledgerRepo;
    this.sharePriceService = sharePriceService;
    this.stockRepo = stockRepo;
    this.stockPriceService = stockPriceService;
    this.tickerBaselineService = tickerBaselineService;
  }

  @Transactional
  public InvestmentPosition buy(
      long buyerHouseId, long targetHouseId, BigDecimal principal, String performer) {
    TradableStock stock =
        stockRepo
            .findHouseStockByHouseIdFetched(targetHouseId)
            .orElseThrow(() -> new IllegalArgumentException("House stock not found"));
    return buyStock(buyerHouseId, stock.getId(), principal, performer);
  }

  @Transactional
  public InvestmentPosition sell(long positionId, long actingBuyerHouseId, String performer) {
    Instant now = Instant.now();
    InvestmentPosition pos = positionRepo.findByIdFetched(positionId).orElseThrow();
    if (!pos.getBuyerHouse().getId().equals(actingBuyerHouseId)) {
      throw new IllegalArgumentException("Position does not belong to this house");
    }
    if (!GoblinConstants.POSITION_OPEN.equals(pos.getStatus())) {
      throw new IllegalArgumentException("Position is not open");
    }
    TradableStock stock = pos.getStock();

    HouseAccount buyer;
    if (stock.getStockType() == StockType.HOUSE) {
      if (stock.getHouseAccount() == null) {
        throw new IllegalStateException("House stock missing houseAccount");
      }
      long buyerId = pos.getBuyerHouse().getId();
      long targetId = stock.getHouseAccount().getId();
      lockOrdered(buyerId, targetId);
      buyer = lockHouse(buyerId);
      lockHouse(targetId);
    } else {
      buyer = lockHouse(pos.getBuyerHouse().getId());
    }

    BigDecimal sellPrice = stockPriceService.currentPrice(stock, now);
    BigDecimal buyPrice = pos.getTargetSharePriceAtBuy();
    BigDecimal payout = pos.getPrincipalAmount().multiply(sellPrice, MC).divide(buyPrice, MC);

    BigDecimal beforeBuyer = buyer.getBalance();
    BigDecimal afterBuyer = beforeBuyer.add(payout).setScale(2, RoundingMode.HALF_UP);
    buyer.setBalance(afterBuyer);
    ledger(buyer, LedgerEntryType.INVESTMENT_SELL, payout, beforeBuyer, afterBuyer, performer);

    pos.setTargetSharePriceAtSell(sellPrice);
    pos.setPayoutAmount(payout.setScale(2, RoundingMode.HALF_UP));
    pos.setStatus(GoblinConstants.POSITION_CLOSED);
    pos.setSoldAt(now);
    positionRepo.save(pos);
    accountRepo.save(buyer);
    tickerBaselineService.syncAllSharePrices(now);
    return pos;
  }

  @Transactional
  public InvestmentPosition buyStock(long buyerHouseId, long stockId, BigDecimal principal, String performer) {
    validatePositive(principal);
    Instant now = Instant.now();

    TradableStock stock =
        stockRepo.findByIdFetched(stockId).orElseThrow(() -> new IllegalArgumentException("Stock not found"));

    HouseAccount targetHouse = null;
    if (stock.getStockType() == StockType.HOUSE) {
      if (stock.getHouseAccount() == null) {
        throw new IllegalStateException("House stock missing houseAccount");
      }
      targetHouse = stock.getHouseAccount();
      if (buyerHouseId == targetHouse.getId()) {
        throw new IllegalArgumentException("Buyer and target must differ");
      }
      lockOrdered(buyerHouseId, targetHouse.getId());
    } else {
      lockHouse(buyerHouseId);
    }

    HouseAccount buyer = lockHouse(buyerHouseId);
    if (targetHouse != null) {
      targetHouse = lockHouse(targetHouse.getId());
    }

    BigDecimal priceAtBuy;
    if (targetHouse != null) {
      var targetOpen =
          positionRepo.findOpenHousePositionsForBuyerFetched(targetHouse.getId(), GoblinConstants.POSITION_OPEN);
      priceAtBuy = sharePriceService.sharePrice(targetHouse, targetOpen, now);
    } else {
      priceAtBuy = stockPriceService.currentPrice(stock, now);
    }
    if (priceAtBuy.abs().compareTo(GoblinConstants.INVESTMENT_PRICE_EPS) < 0) {
      throw new IllegalArgumentException("Target price too close to zero; cannot buy");
    }

    BigDecimal targetWealthAtBuy = targetHouse == null ? BigDecimal.ZERO : targetHouse.getBalance();

    BigDecimal beforeBuyer = buyer.getBalance();
    BigDecimal afterBuyer = beforeBuyer.subtract(principal);
    buyer.setBalance(afterBuyer.setScale(2, RoundingMode.HALF_UP));
    ledger(
        buyer,
        LedgerEntryType.INVESTMENT_BUY,
        principal.negate(),
        beforeBuyer,
        buyer.getBalance(),
        performer);

    InvestmentPosition p = new InvestmentPosition();
    p.setBuyerHouse(buyer);
    p.setStock(stock);
    p.setTargetHouse(targetHouse);
    p.setPrincipalAmount(principal.setScale(2, RoundingMode.HALF_UP));
    p.setTargetWealthAtBuy(targetWealthAtBuy);
    p.setTargetSharePriceAtBuy(priceAtBuy);
    p.setStatus(GoblinConstants.POSITION_OPEN);
    p.setBoughtAt(now);
    positionRepo.save(p);
    accountRepo.save(buyer);
    tickerBaselineService.syncAllSharePrices(now);
    return p;
  }

  private void lockOrdered(long idA, long idB) {
    long lo = Math.min(idA, idB);
    long hi = Math.max(idA, idB);
    accountRepo.findActiveByIdForUpdate(lo).orElseThrow(() -> new IllegalArgumentException("Account not found"));
    accountRepo.findActiveByIdForUpdate(hi).orElseThrow(() -> new IllegalArgumentException("Account not found"));
  }

  private HouseAccount lockHouse(long id) {
    return accountRepo
        .findActiveByIdForUpdate(id)
        .orElseThrow(() -> new IllegalArgumentException("Account not found"));
  }

  private void ledger(
      HouseAccount account,
      String type,
      BigDecimal signedAmount,
      BigDecimal before,
      BigDecimal after,
      String performer) {
    LedgerEntry e = new LedgerEntry();
    e.setAccount(account);
    e.setEntryType(type);
    e.setAmount(signedAmount);
    e.setBeforeBalance(before);
    e.setAfterBalance(after);
    e.setPerformedBy(performer);
    e.setCreatedAt(Instant.now());
    ledgerRepo.save(e);
  }

  private static void validatePositive(BigDecimal p) {
    if (p == null || p.compareTo(BigDecimal.ZERO) <= 0) {
      throw new IllegalArgumentException("Principal must be positive");
    }
  }
}

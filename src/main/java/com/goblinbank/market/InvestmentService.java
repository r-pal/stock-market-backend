package com.goblinbank.market;

import com.goblinbank.GoblinConstants;
import com.goblinbank.account.HouseAccount;
import com.goblinbank.account.HouseAccountRepository;
import com.goblinbank.ledger.LedgerEntry;
import com.goblinbank.ledger.LedgerEntryRepository;
import com.goblinbank.ledger.LedgerEntryType;
import com.goblinbank.shareprice.SharePriceService;
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
  private final TickerBaselineService tickerBaselineService;

  public InvestmentService(
      HouseAccountRepository accountRepo,
      InvestmentPositionRepository positionRepo,
      LedgerEntryRepository ledgerRepo,
      SharePriceService sharePriceService,
      TickerBaselineService tickerBaselineService) {
    this.accountRepo = accountRepo;
    this.positionRepo = positionRepo;
    this.ledgerRepo = ledgerRepo;
    this.sharePriceService = sharePriceService;
    this.tickerBaselineService = tickerBaselineService;
  }

  @Transactional
  public InvestmentPosition buy(
      long buyerHouseId, long targetHouseId, BigDecimal principal, String performer) {
    validatePositive(principal);
    if (buyerHouseId == targetHouseId) {
      throw new IllegalArgumentException("Buyer and target must differ");
    }
    Instant now = Instant.now();
    lockOrdered(buyerHouseId, targetHouseId);
    HouseAccount buyer = lockHouse(buyerHouseId);
    HouseAccount target = lockHouse(targetHouseId);

    var targetOpen =
        positionRepo.findOpenForBuyerFetched(target.getId(), GoblinConstants.POSITION_OPEN);
    BigDecimal targetPriceAtBuy = sharePriceService.sharePrice(target, targetOpen, now);
    if (targetPriceAtBuy.abs().compareTo(GoblinConstants.INVESTMENT_PRICE_EPS) < 0) {
      throw new IllegalArgumentException("Target share price too close to zero; cannot buy");
    }
    BigDecimal targetWealthAtBuy = target.getBalance();

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
    p.setTargetHouse(target);
    p.setPrincipalAmount(principal.setScale(2, RoundingMode.HALF_UP));
    p.setTargetWealthAtBuy(targetWealthAtBuy);
    p.setTargetSharePriceAtBuy(targetPriceAtBuy);
    p.setStatus(GoblinConstants.POSITION_OPEN);
    p.setBoughtAt(now);
    positionRepo.save(p);
    accountRepo.save(buyer);
    tickerBaselineService.syncAllSharePrices(now);
    return p;
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
    long buyerId = pos.getBuyerHouse().getId();
    long targetId = pos.getTargetHouse().getId();
    lockOrdered(buyerId, targetId);
    HouseAccount buyer = lockHouse(buyerId);
    HouseAccount target = lockHouse(targetId);

    var targetOpen =
        positionRepo.findOpenForBuyerFetched(target.getId(), GoblinConstants.POSITION_OPEN);
    BigDecimal targetPriceSell = sharePriceService.sharePrice(target, targetOpen, now);
    BigDecimal buyPrice = pos.getTargetSharePriceAtBuy();
    BigDecimal payout =
        pos.getPrincipalAmount().multiply(targetPriceSell, MC).divide(buyPrice, MC);

    BigDecimal beforeBuyer = buyer.getBalance();
    BigDecimal afterBuyer = beforeBuyer.add(payout).setScale(2, RoundingMode.HALF_UP);
    buyer.setBalance(afterBuyer);
    ledger(buyer, LedgerEntryType.INVESTMENT_SELL, payout, beforeBuyer, afterBuyer, performer);

    pos.setTargetSharePriceAtSell(targetPriceSell);
    pos.setPayoutAmount(payout.setScale(2, RoundingMode.HALF_UP));
    pos.setStatus(GoblinConstants.POSITION_CLOSED);
    pos.setSoldAt(now);
    positionRepo.save(pos);
    accountRepo.save(buyer);
    tickerBaselineService.syncAllSharePrices(now);
    return pos;
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

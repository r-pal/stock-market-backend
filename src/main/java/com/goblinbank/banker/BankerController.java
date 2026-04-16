package com.goblinbank.banker;

import com.goblinbank.account.AccountService;
import com.goblinbank.account.HouseAccount;
import com.goblinbank.config.GlobalInterestConfig;
import com.goblinbank.game.GameAdminService;
import com.goblinbank.game.GameClockConfig;
import com.goblinbank.game.GameClockService;
import com.goblinbank.game.HistoryConfig;
import com.goblinbank.game.HistoryConfigRepository;
import com.goblinbank.ledger.LedgerEntry;
import com.goblinbank.ledger.LedgerEntryRepository;
import com.goblinbank.market.InvestmentPosition;
import com.goblinbank.market.InvestmentDtoMapper;
import com.goblinbank.market.InvestmentPositionRepository;
import com.goblinbank.market.InvestmentService;
import com.goblinbank.shareprice.SharePriceConfig;
import com.goblinbank.shareprice.SharePriceConfigRepository;
import com.goblinbank.stock.StockPriceService;
import com.goblinbank.stock.StockType;
import com.goblinbank.stock.TradableStock;
import com.goblinbank.stock.TradableStockService;
import com.goblinbank.ticker.TickerBaselineService;
import com.goblinbank.web.dto.*;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/banker")
@SecurityRequirement(name = "bearerAuth")
public class BankerController {

  private final AccountService accountService;
  private final InvestmentService investmentService;
  private final InvestmentPositionRepository positionRepo;
  private final SharePriceConfigRepository sharePriceRepo;
  private final HistoryConfigRepository historyRepo;
  private final GameAdminService gameAdminService;
  private final GameClockService gameClockService;
  private final LedgerEntryRepository ledgerRepo;
  private final TickerBaselineService tickerBaselineService;
  private final TradableStockService tradableStockService;
  private final StockPriceService stockPriceService;
  private final PasswordEncoder passwordEncoder;

  public BankerController(
      AccountService accountService,
      InvestmentService investmentService,
      InvestmentPositionRepository positionRepo,
      SharePriceConfigRepository sharePriceRepo,
      HistoryConfigRepository historyRepo,
      GameAdminService gameAdminService,
      GameClockService gameClockService,
      LedgerEntryRepository ledgerRepo,
      TickerBaselineService tickerBaselineService,
      TradableStockService tradableStockService,
      StockPriceService stockPriceService,
      PasswordEncoder passwordEncoder) {
    this.accountService = accountService;
    this.investmentService = investmentService;
    this.positionRepo = positionRepo;
    this.sharePriceRepo = sharePriceRepo;
    this.historyRepo = historyRepo;
    this.gameAdminService = gameAdminService;
    this.gameClockService = gameClockService;
    this.ledgerRepo = ledgerRepo;
    this.tickerBaselineService = tickerBaselineService;
    this.tradableStockService = tradableStockService;
    this.stockPriceService = stockPriceService;
    this.passwordEncoder = passwordEncoder;
  }

  @PostMapping("/accounts")
  public HouseAccount create(@RequestBody CreateHouseRequestDto body, Authentication auth) {
    BigDecimal init = body.initialBalance() == null ? BigDecimal.ZERO : body.initialBalance();
    return accountService.createHouse(body.name(), init, auth.getName());
  }

  @DeleteMapping("/accounts/{id}")
  public void delete(@PathVariable Long id, Authentication auth) {
    accountService.softDeleteHouse(id, auth.getName());
  }

  @PostMapping("/accounts/{id}/deposit")
  public HouseAccount deposit(
      @PathVariable Long id, @RequestBody MoneyAmountDto body, Authentication auth) {
    return accountService.deposit(id, body.amount(), auth.getName());
  }

  @PostMapping("/accounts/{id}/withdraw")
  public HouseAccount withdraw(
      @PathVariable Long id, @RequestBody MoneyAmountDto body, Authentication auth) {
    return accountService.withdraw(id, body.amount(), auth.getName());
  }

  @PutMapping("/accounts/{id}/name")
  public HouseAccount rename(
      @PathVariable Long id, @RequestBody RenameHouseRequestDto body, Authentication auth) {
    return accountService.renameHouse(id, body.name(), auth.getName());
  }

  @PutMapping("/accounts/{id}/portal-password")
  public void portalPassword(
      @PathVariable Long id, @RequestBody PortalPasswordRequestDto body, Authentication auth) {
    if (body.newPassword() == null || body.newPassword().isBlank()) {
      throw new IllegalArgumentException("newPassword required");
    }
    accountService.setPortalPasswordHash(id, passwordEncoder.encode(body.newPassword()));
  }

  @PutMapping("/interest/global")
  public GlobalInterestConfig globalRate(
      @RequestBody GlobalRateRequestDto body, Authentication auth) {
    return accountService.updateGlobalBaseRate(body.baseRatePerHour(), auth.getName());
  }

  @PutMapping("/accounts/{id}/interest-adjustment")
  public HouseAccount houseAdj(
      @PathVariable Long id,
      @RequestBody HouseRateAdjustmentRequestDto body,
      Authentication auth) {
    return accountService.updateHouseRateAdjustment(
        id, body.accountRateAdjustmentPerHour(), auth.getName());
  }

  @PostMapping("/investments/buy")
  public PositionResponseDto bankerBuy(@RequestBody InvestmentBuyRequestDto body, Authentication auth) {
    if (body.buyerHouseId() == null) {
      throw new IllegalArgumentException("buyerHouseId required for banker buy");
    }
    InvestmentPosition p;
    if (body.stockId() != null) {
      p =
          investmentService.buyStock(
              body.buyerHouseId(),
              body.stockId(),
              body.amount(),
              "banker:" + auth.getName());
    } else {
      p =
          investmentService.buy(
              body.buyerHouseId(),
              body.targetHouseId(),
              body.amount(),
              "banker:" + auth.getName());
    }
    return InvestmentDtoMapper.toDto(p);
  }

  @PostMapping("/investments/{positionId}/sell")
  public PositionResponseDto bankerSell(
      @PathVariable Long positionId,
      @RequestBody(required = false) BuyerHintDto hint,
      Authentication auth) {
    InvestmentPosition pos =
        positionRepo
            .findByIdFetched(positionId)
            .orElseThrow(() -> new IllegalArgumentException("Position not found"));
    long buyer =
        hint != null && hint.buyerHouseId() != null
            ? hint.buyerHouseId()
            : pos.getBuyerHouse().getId();
    InvestmentPosition p =
        investmentService.sell(positionId, buyer, "banker:" + auth.getName());
    return InvestmentDtoMapper.toDto(p);
  }

  public record BuyerHintDto(Long buyerHouseId) {}

  @GetMapping("/investments")
  public List<PositionResponseDto> bankerInvestments(
      @RequestParam(required = false) Long houseId,
      @RequestParam(required = false) String status) {
    return positionRepo.searchForBanker(houseId, status).stream()
        .map(InvestmentDtoMapper::toDto)
        .toList();
  }

  @PutMapping("/share-price-config")
  public SharePriceConfig sharePrice(@RequestBody SharePriceConfigPatchDto body, Authentication auth) {
    SharePriceConfig c = sharePriceRepo.findById(1L).orElseThrow();
    if (body.hypeSensitivity() != null) {
      c.setHypeSensitivity(body.hypeSensitivity());
    }
    if (body.interestHorizonHours() != null) {
      if (body.interestHorizonHours() <= 0) {
        throw new IllegalArgumentException("interestHorizonHours must be positive");
      }
      c.setInterestHorizonHours(body.interestHorizonHours());
    }
    if (body.momentumLookbackHours() != null) {
      if (body.momentumLookbackHours() <= 0) {
        throw new IllegalArgumentException("momentumLookbackHours must be positive");
      }
      c.setMomentumLookbackHours(body.momentumLookbackHours());
    }
    c.setUpdatedBy("banker:" + auth.getName());
    c.setUpdatedAt(Instant.now());
    sharePriceRepo.save(c);
    tickerBaselineService.syncAllSharePrices(Instant.now());
    return c;
  }

  @PutMapping("/share-price-config/hype")
  public SharePriceConfig hypeOnly(@RequestBody SharePriceConfigPatchDto body, Authentication auth) {
    return sharePrice(
        new SharePriceConfigPatchDto(body.hypeSensitivity(), null, null), auth);
  }

  @PutMapping("/share-price-config/momentum-lookback-hours")
  public SharePriceConfig momentumOnly(
      @RequestBody SharePriceConfigPatchDto body, Authentication auth) {
    return sharePrice(
        new SharePriceConfigPatchDto(null, null, body.momentumLookbackHours()), auth);
  }

  @PutMapping("/history-config")
  public HistoryConfig historyCfg(@RequestBody HistoryConfigPatchDto body, Authentication auth) {
    Integer interval = body.snapshotIntervalMinutes();
    if (interval == null || interval <= 0 || 60 % interval != 0) {
      throw new IllegalArgumentException("snapshotIntervalMinutes must divide 60 evenly");
    }
    HistoryConfig c = historyRepo.findById(1L).orElseThrow();
    c.setSnapshotIntervalMinutes(interval);
    c.setUpdatedBy("banker:" + auth.getName());
    c.setUpdatedAt(Instant.now());
    historyRepo.save(c);
    tickerBaselineService.syncAllSharePrices(Instant.now());
    return c;
  }

  @PutMapping("/game/config")
  public GameClockConfig gameCfg(@RequestBody GameDurationRequestDto body, Authentication auth) {
    return gameAdminService.setDurationMinutes(body.gameDurationMinutes(), auth.getName());
  }

  @PostMapping("/game/start")
  public GameClockConfig gameStart(Authentication auth) {
    return gameAdminService.startGame(auth.getName());
  }

  @GetMapping("/game/status")
  public GameStatusResponseDto gameStatus() {
    GameClockConfig g = gameAdminService.getConfig();
    Instant now = Instant.now();
    int elapsed = gameClockService.currentElapsedMinutes(now);
    int remaining = Math.max(0, g.getGameDurationMinutes() - elapsed);
    return new GameStatusResponseDto(
        g.getGameStartAt(), g.getGameDurationMinutes(), elapsed, remaining);
  }

  @GetMapping("/ledger")
  public List<LedgerLineResponseDto> ledger(
      @RequestParam(required = false) Long accountId, @RequestParam(defaultValue = "0") int page) {
    Page<LedgerEntry> p =
        ledgerRepo.search(accountId, PageRequest.of(page, 100));
    return p.getContent().stream().map(this::mapLedger).toList();
  }

  // Pawn shop (banker-managed item stocks + visibility into all stocks/prices)
  @GetMapping("/pawn-shop/stocks")
  public List<TradableStockResponseDto> pawnShopStocks() {
    Instant now = Instant.now();
    return tradableStockService.listActive().stream().map(s -> mapStock(s, now)).toList();
  }

  @PostMapping("/pawn-shop/stocks")
  public TradableStockResponseDto pawnShopCreate(
      @RequestBody PawnShopCreateStockRequestDto body, Authentication auth) {
    TradableStock s =
        tradableStockService.createItemStock(
            body.displayName(), body.currentPrice(), "banker:" + auth.getName());
    return mapStock(s, Instant.now());
  }

  @PutMapping("/pawn-shop/stocks/{id}/price")
  public TradableStockResponseDto pawnShopReprice(
      @PathVariable Long id, @RequestBody PawnShopRepriceRequestDto body, Authentication auth) {
    TradableStock s =
        tradableStockService.updateItemPrice(id, body.newPrice(), "banker:" + auth.getName());
    return mapStock(s, Instant.now());
  }

  private TradableStockResponseDto mapStock(TradableStock s, Instant now) {
    BigDecimal price =
        s.getStockType() == StockType.HOUSE ? stockPriceService.currentPrice(s, now) : s.getCurrentPrice();
    Long houseId = s.getHouseAccount() == null ? null : s.getHouseAccount().getId();
    return new TradableStockResponseDto(
        s.getId(), s.getDisplayName(), s.getStockType().name(), houseId, price, s.isActive());
  }

  private LedgerLineResponseDto mapLedger(LedgerEntry e) {
    return new LedgerLineResponseDto(
        e.getId(),
        e.getAccount().getId(),
        e.getEntryType(),
        e.getAmount(),
        e.getBeforeBalance(),
        e.getAfterBalance(),
        e.getPerformedBy(),
        e.getCreatedAt());
  }

}

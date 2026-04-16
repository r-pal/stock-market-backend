package com.goblinbank.account;

import com.goblinbank.GoblinConstants;
import com.goblinbank.audit.HouseRenameAudit;
import com.goblinbank.audit.HouseRenameAuditRepository;
import com.goblinbank.config.GlobalInterestConfig;
import com.goblinbank.config.GlobalInterestConfigRepository;
import com.goblinbank.config.RateBoundsProperties;
import com.goblinbank.ledger.LedgerEntry;
import com.goblinbank.ledger.LedgerEntryRepository;
import com.goblinbank.ledger.LedgerEntryType;
import com.goblinbank.market.InvestmentPositionRepository;
import com.goblinbank.stock.StockType;
import com.goblinbank.stock.TradableStock;
import com.goblinbank.stock.TradableStockRepository;
import com.goblinbank.ticker.TickerBaselineService;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AccountService {

  private final HouseAccountRepository accountRepo;
  private final LedgerEntryRepository ledgerRepo;
  private final InvestmentPositionRepository positionRepo;
  private final TickerBaselineService tickerBaselineService;
  private final TradableStockRepository stockRepo;
  private final GlobalInterestConfigRepository globalInterestRepo;
  private final RateBoundsProperties rateBounds;
  private final HouseRenameAuditRepository renameAuditRepo;

  public AccountService(
      HouseAccountRepository accountRepo,
      LedgerEntryRepository ledgerRepo,
      InvestmentPositionRepository positionRepo,
      TickerBaselineService tickerBaselineService,
      TradableStockRepository stockRepo,
      GlobalInterestConfigRepository globalInterestRepo,
      RateBoundsProperties rateBounds,
      HouseRenameAuditRepository renameAuditRepo) {
    this.accountRepo = accountRepo;
    this.ledgerRepo = ledgerRepo;
    this.positionRepo = positionRepo;
    this.tickerBaselineService = tickerBaselineService;
    this.stockRepo = stockRepo;
    this.globalInterestRepo = globalInterestRepo;
    this.rateBounds = rateBounds;
    this.renameAuditRepo = renameAuditRepo;
  }

  @Transactional
  public HouseAccount deposit(Long accountId, BigDecimal amount, String bankerUsername) {
    validatePositiveAmount(amount);
    HouseAccount a = loadActiveForUpdate(accountId);
    applyBalanceDelta(a, amount, LedgerEntryType.DEPOSIT, performerBanker(bankerUsername));
    accountRepo.save(a);
    tickerBaselineService.syncAllSharePrices(Instant.now());
    return a;
  }

  @Transactional
  public HouseAccount withdraw(Long accountId, BigDecimal amount, String bankerUsername) {
    validatePositiveAmount(amount);
    HouseAccount a = loadActiveForUpdate(accountId);
    applyBalanceDelta(a, amount.negate(), LedgerEntryType.WITHDRAW, performerBanker(bankerUsername));
    accountRepo.save(a);
    tickerBaselineService.syncAllSharePrices(Instant.now());
    return a;
  }

  @Transactional
  public HouseAccount createHouse(String name, BigDecimal initialBalance, String bankerUsername) {
    String trimmed = validateHouseName(name);
    if (accountRepo.existsByDeletedAtIsNullAndHouseNameIgnoreCase(trimmed)) {
      throw new IllegalArgumentException("House name already exists");
    }
    validateNonNegative(initialBalance);
    HouseAccount h = new HouseAccount();
    h.setHouseName(trimmed);
    h.setBalance(BigDecimal.ZERO.setScale(2, java.math.RoundingMode.HALF_UP));
    h.setAccountRateAdjustmentPerHour(BigDecimal.ZERO);
    accountRepo.save(h);

    TradableStock houseStock = new TradableStock();
    houseStock.setDisplayName(trimmed);
    houseStock.setStockType(StockType.HOUSE);
    houseStock.setHouseAccount(h);
    houseStock.setCurrentPrice(null);
    houseStock.setActive(true);
    Instant now = Instant.now();
    houseStock.setCreatedBy(performerBanker(bankerUsername));
    houseStock.setCreatedAt(now);
    houseStock.setUpdatedBy(performerBanker(bankerUsername));
    houseStock.setUpdatedAt(now);
    stockRepo.save(houseStock);

    if (initialBalance.compareTo(BigDecimal.ZERO) != 0) {
      applyBalanceDelta(
          h, initialBalance, LedgerEntryType.DEPOSIT, performerBanker(bankerUsername));
      accountRepo.save(h);
    }
    tickerBaselineService.syncAllSharePrices(Instant.now());
    return h;
  }

  @Transactional
  public void softDeleteHouse(Long accountId, String bankerUsername) {
    HouseAccount a = loadActiveForUpdate(accountId);
    if (a.getBalance().compareTo(BigDecimal.ZERO) != 0) {
      throw new IllegalArgumentException("House balance must be zero before delete");
    }
    long open =
        positionRepo.countInvolvingHouseAndStatus(accountId, GoblinConstants.POSITION_OPEN);
    if (open > 0) {
      throw new IllegalArgumentException("Cannot delete house with open investment positions");
    }
    a.setDeletedAt(Instant.now());
    accountRepo.save(a);
    stockRepo
        .findHouseStockByHouseIdFetched(a.getId())
        .ifPresent(
            s -> {
              s.setActive(false);
              s.setUpdatedBy(performerBanker(bankerUsername));
              s.setUpdatedAt(Instant.now());
              stockRepo.save(s);
            });
    tickerBaselineService.syncAllSharePrices(Instant.now());
  }

  @Transactional
  public HouseAccount renameHouse(Long accountId, String newName, String bankerUsername) {
    HouseAccount a = loadActiveForUpdate(accountId);
    String trimmed = validateHouseName(newName);
    if (!trimmed.equalsIgnoreCase(a.getHouseName())
        && accountRepo.existsByDeletedAtIsNullAndHouseNameIgnoreCase(trimmed)) {
      throw new IllegalArgumentException("House name already exists");
    }
    HouseRenameAudit audit = new HouseRenameAudit();
    audit.setHouse(a);
    audit.setOldName(a.getHouseName());
    audit.setNewName(trimmed);
    audit.setChangedBy(performerBanker(bankerUsername));
    renameAuditRepo.save(audit);
    a.setHouseName(trimmed);
    accountRepo.save(a);
    stockRepo
        .findHouseStockByHouseIdFetched(a.getId())
        .ifPresent(
            s -> {
              s.setDisplayName(trimmed);
              s.setUpdatedBy(performerBanker(bankerUsername));
              s.setUpdatedAt(Instant.now());
              stockRepo.save(s);
            });
    tickerBaselineService.syncAllSharePrices(Instant.now());
    return a;
  }

  @Transactional
  public void setPortalPasswordHash(Long accountId, String bcryptHash) {
    HouseAccount a = loadActiveForUpdate(accountId);
    a.setPortalPasswordHash(bcryptHash);
    a.setPortalPasswordUpdatedAt(Instant.now());
    accountRepo.save(a);
  }

  @Transactional
  public GlobalInterestConfig updateGlobalBaseRate(BigDecimal newRate, String bankerUsername) {
    validateHourlyRate(newRate);
    List<HouseAccount> houses = accountRepo.findAllActive();
    for (HouseAccount h : houses) {
      validateHourlyRate(newRate.add(h.getAccountRateAdjustmentPerHour()));
    }
    GlobalInterestConfig g = globalInterestRepo.findById(1L).orElseThrow();
    g.setBaseRatePerHour(newRate);
    g.setUpdatedBy(performerBanker(bankerUsername));
    g.setUpdatedAt(Instant.now());
    globalInterestRepo.save(g);
    tickerBaselineService.syncAllSharePrices(Instant.now());
    return g;
  }

  @Transactional
  public HouseAccount updateHouseRateAdjustment(
      Long accountId, BigDecimal adjustment, String bankerUsername) {
    BigDecimal base = globalInterestRepo.findById(1L).orElseThrow().getBaseRatePerHour();
    validateHourlyRate(base.add(adjustment));
    HouseAccount a = loadActiveForUpdate(accountId);
    a.setAccountRateAdjustmentPerHour(adjustment);
    accountRepo.save(a);
    tickerBaselineService.syncAllSharePrices(Instant.now());
    return a;
  }

  private void validateHourlyRate(BigDecimal effectiveOrComponent) {
    if (effectiveOrComponent.compareTo(rateBounds.getMinHourly()) < 0
        || effectiveOrComponent.compareTo(rateBounds.getMaxHourly()) > 0) {
      throw new IllegalArgumentException("Rate out of allowed bounds");
    }
  }

  private HouseAccount loadActiveForUpdate(Long id) {
    return accountRepo.findActiveByIdForUpdate(id).orElseThrow(() -> new IllegalArgumentException("Account not found"));
  }

  private static void validatePositiveAmount(BigDecimal amount) {
    if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
      throw new IllegalArgumentException("Amount must be positive");
    }
  }

  private static void validateNonNegative(BigDecimal amount) {
    if (amount == null || amount.compareTo(BigDecimal.ZERO) < 0) {
      throw new IllegalArgumentException("Balance must be non-negative");
    }
  }

  private static String validateHouseName(String name) {
    if (name == null) {
      throw new IllegalArgumentException("House name required");
    }
    String t = name.trim();
    if (t.isEmpty() || t.length() > 100) {
      throw new IllegalArgumentException("House name must be 1..100 characters");
    }
    return t;
  }

  private void applyBalanceDelta(
      HouseAccount account, BigDecimal delta, String type, String performer) {
    BigDecimal before = account.getBalance();
    BigDecimal after = before.add(delta).setScale(2, java.math.RoundingMode.HALF_UP);
    account.setBalance(after);
    LedgerEntry e = new LedgerEntry();
    e.setAccount(account);
    e.setEntryType(type);
    e.setAmount(delta);
    e.setBeforeBalance(before);
    e.setAfterBalance(after);
    e.setPerformedBy(performer);
    e.setCreatedAt(Instant.now());
    ledgerRepo.save(e);
  }

  private static String performerBanker(String username) {
    return "banker:" + username;
  }
}

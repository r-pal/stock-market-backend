package com.goblinbank.interest;

import com.goblinbank.account.HouseAccount;
import com.goblinbank.account.HouseAccountRepository;
import com.goblinbank.config.GlobalInterestConfigRepository;
import com.goblinbank.ledger.LedgerEntry;
import com.goblinbank.ledger.LedgerEntryRepository;
import com.goblinbank.ledger.LedgerEntryType;
import com.goblinbank.ticker.TickerBaselineService;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InterestService {

  private static final MathContext MC = new MathContext(24, RoundingMode.HALF_UP);

  private final HouseAccountRepository accountRepo;
  private final GlobalInterestConfigRepository globalInterestRepo;
  private final LedgerEntryRepository ledgerRepo;
  private final TickerBaselineService tickerBaselineService;

  public InterestService(
      HouseAccountRepository accountRepo,
      GlobalInterestConfigRepository globalInterestRepo,
      LedgerEntryRepository ledgerRepo,
      TickerBaselineService tickerBaselineService) {
    this.accountRepo = accountRepo;
    this.globalInterestRepo = globalInterestRepo;
    this.ledgerRepo = ledgerRepo;
    this.tickerBaselineService = tickerBaselineService;
  }

  @Transactional
  public void accrueHour(Instant now) {
    var base = globalInterestRepo.findById(1L).orElseThrow().getBaseRatePerHour();
    List<HouseAccount> houses = accountRepo.findAllActive();
    houses.sort((a, b) -> Long.compare(a.getId(), b.getId()));
    for (HouseAccount h : houses) {
      HouseAccount locked = accountRepo.findActiveByIdForUpdate(h.getId()).orElseThrow();
      BigDecimal rate = base.add(locked.getAccountRateAdjustmentPerHour());
      BigDecimal interest =
          locked.getBalance().multiply(rate, MC).setScale(2, RoundingMode.HALF_UP);
      if (interest.compareTo(BigDecimal.ZERO) == 0) {
        continue;
      }
      BigDecimal before = locked.getBalance();
      BigDecimal after = before.add(interest);
      locked.setBalance(after);
      LedgerEntry e = new LedgerEntry();
      e.setAccount(locked);
      e.setEntryType(LedgerEntryType.INTEREST_ACCRUAL);
      e.setAmount(interest);
      e.setBeforeBalance(before);
      e.setAfterBalance(after);
      e.setPerformedBy("scheduler:hourly-interest");
      e.setCreatedAt(now);
      ledgerRepo.save(e);
      accountRepo.save(locked);
    }
    tickerBaselineService.syncAllSharePrices(now);
  }
}

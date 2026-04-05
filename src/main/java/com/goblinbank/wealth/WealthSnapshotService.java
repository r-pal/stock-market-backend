package com.goblinbank.wealth;

import com.goblinbank.GoblinConstants;
import com.goblinbank.account.HouseAccount;
import com.goblinbank.account.HouseAccountRepository;
import com.goblinbank.game.GameClockService;
import com.goblinbank.game.HistoryConfigRepository;
import com.goblinbank.market.InvestmentPositionRepository;
import com.goblinbank.shareprice.SharePriceService;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WealthSnapshotService {

  private final HouseAccountRepository accountRepo;
  private final HouseWealthSnapshotRepository snapshotRepo;
  private final HistoryConfigRepository historyConfigRepo;
  private final GameClockService gameClock;
  private final SharePriceService sharePriceService;
  private final InvestmentPositionRepository positionRepo;

  public WealthSnapshotService(
      HouseAccountRepository accountRepo,
      HouseWealthSnapshotRepository snapshotRepo,
      HistoryConfigRepository historyConfigRepo,
      GameClockService gameClock,
      SharePriceService sharePriceService,
      InvestmentPositionRepository positionRepo) {
    this.accountRepo = accountRepo;
    this.snapshotRepo = snapshotRepo;
    this.historyConfigRepo = historyConfigRepo;
    this.gameClock = gameClock;
    this.sharePriceService = sharePriceService;
    this.positionRepo = positionRepo;
  }

  /** Whether this clock minute aligns with the configured snapshot grid (UTC). */
  @Transactional(readOnly = true)
  public boolean shouldCaptureThisMinute(Instant now) {
    if (!gameClock.isStarted()) {
      return false;
    }
    int interval = historyConfigRepo.findById(1L).orElseThrow().getSnapshotIntervalMinutes();
    if (interval <= 0 || 60 % interval != 0) {
      return false;
    }
    ZonedDateTime z = now.atZone(ZoneOffset.UTC).truncatedTo(ChronoUnit.MINUTES);
    return z.getMinute() % interval == 0;
  }

  @Transactional
  public void captureAlignedMinute(Instant capturedAt) {
    if (!gameClock.isStarted()) {
      return;
    }
    int minutesElapsed = gameClock.currentElapsedMinutes(capturedAt);
    List<HouseAccount> houses = accountRepo.findAllActive();
    for (HouseAccount house : houses) {
      if (snapshotRepo.existsByHouse_IdAndCapturedAt(house.getId(), capturedAt)) {
        continue;
      }
      var open =
          positionRepo.findOpenForBuyerFetched(house.getId(), GoblinConstants.POSITION_OPEN);
      BigDecimal price = sharePriceService.sharePrice(house, open, capturedAt);
      BigDecimal rate = sharePriceService.effectiveRate(house);
      HouseWealthSnapshot snap = new HouseWealthSnapshot();
      snap.setHouse(house);
      snap.setCapturedAt(capturedAt);
      snap.setMinutes(minutesElapsed);
      snap.setHouseName(house.getHouseName());
      snap.setBalance(house.getBalance());
      snap.setSharePrice(price);
      snap.setEffectiveRatePerHour(rate);
      snapshotRepo.save(snap);
    }
  }
}

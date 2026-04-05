package com.goblinbank.shareprice;

import com.goblinbank.GoblinConstants;
import com.goblinbank.account.HouseAccount;
import com.goblinbank.config.GlobalInterestConfig;
import com.goblinbank.config.GlobalInterestConfigRepository;
import com.goblinbank.game.GameClockService;
import com.goblinbank.game.HistoryConfig;
import com.goblinbank.game.HistoryConfigRepository;
import com.goblinbank.market.InvestmentPosition;
import com.goblinbank.wealth.HouseWealthSnapshot;
import com.goblinbank.wealth.HouseWealthSnapshotRepository;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SharePriceService {

  private static final MathContext MC = new MathContext(24, RoundingMode.HALF_UP);

  private final GlobalInterestConfigRepository interestRepo;
  private final SharePriceConfigRepository shareCfgRepo;
  private final HistoryConfigRepository historyCfgRepo;
  private final HouseWealthSnapshotRepository snapshotRepo;
  private final GameClockService gameClock;

  public SharePriceService(
      GlobalInterestConfigRepository interestRepo,
      SharePriceConfigRepository shareCfgRepo,
      HistoryConfigRepository historyCfgRepo,
      HouseWealthSnapshotRepository snapshotRepo,
      GameClockService gameClock) {
    this.interestRepo = interestRepo;
    this.shareCfgRepo = shareCfgRepo;
    this.historyCfgRepo = historyCfgRepo;
    this.snapshotRepo = snapshotRepo;
    this.gameClock = gameClock;
  }

  @Transactional(readOnly = true)
  public BigDecimal effectiveRate(HouseAccount house) {
    GlobalInterestConfig g = interestRepo.findById(1L).orElseThrow();
    return g.getBaseRatePerHour().add(house.getAccountRateAdjustmentPerHour());
  }

  @Transactional(readOnly = true)
  public BigDecimal holdingsValue(HouseAccount buyer, List<InvestmentPosition> openAsBuyer) {
    BigDecimal h = BigDecimal.ZERO;
    for (InvestmentPosition p : openAsBuyer) {
      HouseAccount target = p.getTargetHouse();
      BigDecimal w0 =
          p.getTargetWealthAtBuy().abs().compareTo(GoblinConstants.INVESTMENT_PRICE_EPS) < 0
              ? GoblinConstants.INVESTMENT_PRICE_EPS
              : p.getTargetWealthAtBuy().abs();
      BigDecimal ratio = target.getBalance().divide(w0, MC);
      h = h.add(p.getPrincipalAmount().multiply(ratio, MC));
    }
    return h.setScale(2, RoundingMode.HALF_UP);
  }

  @Transactional(readOnly = true)
  public BigDecimal momentum(HouseAccount house, Instant now) {
    SharePriceConfig sp = shareCfgRepo.findById(1L).orElseThrow();
    HistoryConfig hc = historyCfgRepo.findById(1L).orElseThrow();
    int interval = hc.getSnapshotIntervalMinutes();
    int L = sp.getMomentumLookbackHours();
    if (L <= 0 || 60 % interval != 0) {
      return BigDecimal.ZERO;
    }
    if (!gameClock.isStarted()) {
      return BigDecimal.ZERO;
    }
    int curMin = gameClock.currentElapsedMinutes(now);
    int targetPast = curMin - (L * 60);
    if (targetPast < 0) {
      return BigDecimal.ZERO;
    }
    List<HouseWealthSnapshot> snaps =
        snapshotRepo.findLatestForHouseUpToMinute(house.getId(), targetPast);
    if (snaps.isEmpty()) {
      return BigDecimal.ZERO;
    }
    HouseWealthSnapshot then = snaps.get(0);
    BigDecimal wNow = house.getBalance();
    BigDecimal wThen = then.getBalance();
    BigDecimal denom = wThen.abs();
    if (denom.compareTo(GoblinConstants.WEALTH_MOMENTUM_EPS) < 0) {
      denom = GoblinConstants.WEALTH_MOMENTUM_EPS;
    }
    return wNow.subtract(wThen).divide(denom, 12, RoundingMode.HALF_UP);
  }

  @Transactional(readOnly = true)
  public BigDecimal sharePrice(
      HouseAccount house, List<InvestmentPosition> openAsBuyer, Instant now) {
    SharePriceConfig sp = shareCfgRepo.findById(1L).orElseThrow();
    BigDecimal w = house.getBalance();
    BigDecimal h = holdingsValue(house, openAsBuyer);
    BigDecimal r = effectiveRate(house);
    BigDecimal m = momentum(house, now);
    BigDecimal k = new BigDecimal(sp.getInterestHorizonHours());
    BigDecimal onePlusKr = BigDecimal.ONE.add(k.multiply(r, MC), MC);
    BigDecimal onePlusHm = BigDecimal.ONE.add(sp.getHypeSensitivity().multiply(m, MC), MC);
    BigDecimal num = w.add(h, MC).multiply(onePlusKr, MC).multiply(onePlusHm, MC);
    return num.divide(new BigDecimal(GoblinConstants.SHARES_OUTSTANDING), 6, RoundingMode.HALF_UP);
  }
}

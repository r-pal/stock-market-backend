package com.goblinbank.ticker;

import com.goblinbank.GoblinConstants;
import com.goblinbank.account.HouseAccount;
import com.goblinbank.account.HouseAccountRepository;
import com.goblinbank.market.InvestmentPositionRepository;
import com.goblinbank.shareprice.SharePriceService;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TickerBaselineService {

  private final HouseAccountRepository accountRepo;
  private final InvestmentPositionRepository positionRepo;
  private final SharePriceService sharePriceService;
  private final HouseTickerBaselineRepository baselineRepo;

  public TickerBaselineService(
      HouseAccountRepository accountRepo,
      InvestmentPositionRepository positionRepo,
      SharePriceService sharePriceService,
      HouseTickerBaselineRepository baselineRepo) {
    this.accountRepo = accountRepo;
    this.positionRepo = positionRepo;
    this.sharePriceService = sharePriceService;
    this.baselineRepo = baselineRepo;
  }

  /** Recompute and store last committed share price for every active house (after mutations). */
  @Transactional
  public void syncAllSharePrices(Instant now) {
    List<HouseAccount> houses = accountRepo.findAllActive();
    for (HouseAccount house : houses) {
      var open =
          positionRepo.findOpenHousePositionsForBuyerFetched(house.getId(), GoblinConstants.POSITION_OPEN);
      BigDecimal price = sharePriceService.sharePrice(house, open, now);
      upsert(house, price, now);
    }
  }

  private void upsert(HouseAccount house, BigDecimal price, Instant now) {
    HouseTickerBaseline row =
        baselineRepo
            .findById(house.getId())
            .orElseGet(
                () -> {
                  HouseTickerBaseline b = new HouseTickerBaseline();
                  b.setHouse(house);
                  return b;
                });
    row.setLastSharePrice(price);
    row.setUpdatedAt(now);
    baselineRepo.save(row);
  }
}

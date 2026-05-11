package com.goblinbank.ticker;

import com.goblinbank.GoblinConstants;
import com.goblinbank.account.HouseAccount;
import com.goblinbank.account.HouseAccountRepository;
import com.goblinbank.market.InvestmentPositionRepository;
import com.goblinbank.shareprice.SharePriceService;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TickerFormatterService {

  private final HouseAccountRepository accountRepo;
  private final InvestmentPositionRepository positionRepo;
  private final SharePriceService sharePriceService;

  public TickerFormatterService(
      HouseAccountRepository accountRepo,
      InvestmentPositionRepository positionRepo,
      SharePriceService sharePriceService) {
    this.accountRepo = accountRepo;
    this.positionRepo = positionRepo;
    this.sharePriceService = sharePriceService;
  }

  @Transactional(readOnly = true)
  public String buildMessage(Instant now) {
    List<HouseAccount> houses = accountRepo.findAllActive();
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < houses.size(); i++) {
      HouseAccount h = houses.get(i);
      if (i > 0) {
        sb.append(" | ");
      }
      sb.append(segment(h, now));
    }
    return sb.toString();
  }

  private String segment(HouseAccount house, Instant now) {
    var open =
        positionRepo.findOpenHousePositionsForBuyerFetched(house.getId(), GoblinConstants.POSITION_OPEN);
    BigDecimal pNow = sharePriceService.sharePrice(house, open, now);
    String priceStr = pNow.setScale(2, RoundingMode.HALF_UP).toPlainString();
    return house.getHouseName() + " " + GoblinConstants.CURRENCY_SYMBOL + priceStr;
  }
}

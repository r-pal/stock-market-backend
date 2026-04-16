package com.goblinbank.stock;

import com.goblinbank.GoblinConstants;
import com.goblinbank.market.InvestmentPositionRepository;
import com.goblinbank.shareprice.SharePriceService;
import java.math.BigDecimal;
import java.time.Instant;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StockPriceService {

  private final SharePriceService sharePriceService;
  private final InvestmentPositionRepository positionRepo;

  public StockPriceService(SharePriceService sharePriceService, InvestmentPositionRepository positionRepo) {
    this.sharePriceService = sharePriceService;
    this.positionRepo = positionRepo;
  }

  @Transactional(readOnly = true)
  public BigDecimal currentPrice(TradableStock stock, Instant now) {
    if (stock == null) {
      throw new IllegalArgumentException("Stock required");
    }
    if (!stock.isActive()) {
      throw new IllegalArgumentException("Stock is inactive");
    }
    if (stock.getStockType() == StockType.ITEM) {
      if (stock.getCurrentPrice() == null) {
        throw new IllegalStateException("Item stock has no current price");
      }
      return stock.getCurrentPrice();
    }
    if (stock.getStockType() != StockType.HOUSE || stock.getHouseAccount() == null) {
      throw new IllegalStateException("House stock missing houseAccount");
    }
    var open =
        positionRepo.findOpenHousePositionsForBuyerFetched(
            stock.getHouseAccount().getId(), GoblinConstants.POSITION_OPEN);
    return sharePriceService.sharePrice(stock.getHouseAccount(), open, now);
  }
}


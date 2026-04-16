package com.goblinbank.stock;

import com.goblinbank.web.dto.TradableStockResponseDto;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/public/stocks")
public class PublicStockController {

  private final TradableStockService tradableStockService;
  private final StockPriceService stockPriceService;

  public PublicStockController(TradableStockService tradableStockService, StockPriceService stockPriceService) {
    this.tradableStockService = tradableStockService;
    this.stockPriceService = stockPriceService;
  }

  @GetMapping
  public List<TradableStockResponseDto> list() {
    Instant now = Instant.now();
    return tradableStockService.listActive().stream().map(s -> map(s, now)).toList();
  }

  private TradableStockResponseDto map(TradableStock s, Instant now) {
    BigDecimal price =
        s.getStockType() == StockType.HOUSE ? stockPriceService.currentPrice(s, now) : s.getCurrentPrice();
    Long houseId = s.getHouseAccount() == null ? null : s.getHouseAccount().getId();
    return new TradableStockResponseDto(
        s.getId(), s.getDisplayName(), s.getStockType().name(), houseId, price, s.isActive());
  }
}


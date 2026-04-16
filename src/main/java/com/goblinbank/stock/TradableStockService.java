package com.goblinbank.stock;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TradableStockService {

  private final TradableStockRepository stockRepo;

  public TradableStockService(TradableStockRepository stockRepo) {
    this.stockRepo = stockRepo;
  }

  @Transactional(readOnly = true)
  public List<TradableStock> listActive() {
    return stockRepo.findAllActiveFetched();
  }

  @Transactional
  public TradableStock createItemStock(String displayName, BigDecimal currentPrice, String performer) {
    if (displayName == null || displayName.isBlank()) {
      throw new IllegalArgumentException("displayName required");
    }
    if (currentPrice == null) {
      throw new IllegalArgumentException("currentPrice required");
    }
    if (stockRepo.existsActiveDisplayName(displayName)) {
      throw new IllegalArgumentException("Stock displayName already exists");
    }
    Instant now = Instant.now();
    TradableStock s = new TradableStock();
    s.setDisplayName(displayName.trim());
    s.setStockType(StockType.ITEM);
    s.setCurrentPrice(currentPrice);
    s.setActive(true);
    s.setCreatedBy(performer);
    s.setCreatedAt(now);
    s.setUpdatedBy(performer);
    s.setUpdatedAt(now);
    return stockRepo.save(s);
  }

  @Transactional
  public TradableStock updateItemPrice(long stockId, BigDecimal newPrice, String performer) {
    if (newPrice == null) {
      throw new IllegalArgumentException("newPrice required");
    }
    TradableStock s =
        stockRepo.findByIdFetched(stockId).orElseThrow(() -> new IllegalArgumentException("Stock not found"));
    if (s.getStockType() != StockType.ITEM) {
      throw new IllegalArgumentException("Only item stocks can be repriced manually");
    }
    s.setCurrentPrice(newPrice);
    s.setUpdatedBy(performer);
    s.setUpdatedAt(Instant.now());
    return stockRepo.save(s);
  }
}


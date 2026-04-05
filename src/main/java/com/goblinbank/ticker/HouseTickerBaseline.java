package com.goblinbank.ticker;

import com.goblinbank.account.HouseAccount;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "house_ticker_baseline")
public class HouseTickerBaseline {
  @Id
  @Column(name = "house_id")
  private Long houseId;

  @OneToOne(fetch = FetchType.LAZY)
  @MapsId
  @JoinColumn(name = "house_id")
  private HouseAccount house;

  @Column(name = "last_share_price", nullable = false, precision = 19, scale = 6)
  private BigDecimal lastSharePrice;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  public Long getHouseId() {
    return houseId;
  }

  public BigDecimal getLastSharePrice() {
    return lastSharePrice;
  }

  public void setLastSharePrice(BigDecimal lastSharePrice) {
    this.lastSharePrice = lastSharePrice;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }

  public void setUpdatedAt(Instant updatedAt) {
    this.updatedAt = updatedAt;
  }

  public HouseAccount getHouse() {
    return house;
  }

  public void setHouse(HouseAccount house) {
    this.house = house;
  }
}

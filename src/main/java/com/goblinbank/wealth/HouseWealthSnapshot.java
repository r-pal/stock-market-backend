package com.goblinbank.wealth;

import com.goblinbank.account.HouseAccount;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "house_wealth_snapshot")
public class HouseWealthSnapshot {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  @JoinColumn(name = "house_id", nullable = false)
  private HouseAccount house;

  @Column(name = "captured_at", nullable = false)
  private Instant capturedAt;

  @Column(name = "minutes", nullable = false)
  private Integer minutes;

  @Column(name = "house_name")
  private String houseName;

  @Column(name = "balance", nullable = false, precision = 19, scale = 2)
  private BigDecimal balance;

  @Column(name = "share_price", nullable = false, precision = 19, scale = 6)
  private BigDecimal sharePrice;

  @Column(name = "effective_rate_per_hour", nullable = false, precision = 12, scale = 8)
  private BigDecimal effectiveRatePerHour;

  public Long getId() {
    return id;
  }

  public HouseAccount getHouse() {
    return house;
  }

  public void setHouse(HouseAccount house) {
    this.house = house;
  }

  public Instant getCapturedAt() {
    return capturedAt;
  }

  public void setCapturedAt(Instant capturedAt) {
    this.capturedAt = capturedAt;
  }

  public Integer getMinutes() {
    return minutes;
  }

  public void setMinutes(Integer minutes) {
    this.minutes = minutes;
  }

  public String getHouseName() {
    return houseName;
  }

  public void setHouseName(String houseName) {
    this.houseName = houseName;
  }

  public BigDecimal getBalance() {
    return balance;
  }

  public void setBalance(BigDecimal balance) {
    this.balance = balance;
  }

  public BigDecimal getSharePrice() {
    return sharePrice;
  }

  public void setSharePrice(BigDecimal sharePrice) {
    this.sharePrice = sharePrice;
  }

  public BigDecimal getEffectiveRatePerHour() {
    return effectiveRatePerHour;
  }

  public void setEffectiveRatePerHour(BigDecimal effectiveRatePerHour) {
    this.effectiveRatePerHour = effectiveRatePerHour;
  }
}


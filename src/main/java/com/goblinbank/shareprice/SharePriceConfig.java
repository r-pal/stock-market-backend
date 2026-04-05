package com.goblinbank.shareprice;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "share_price_config")
public class SharePriceConfig {
  @Id private Long id;

  @Column(name = "hype_sensitivity", nullable = false, precision = 12, scale = 8)
  private BigDecimal hypeSensitivity;

  @Column(name = "interest_horizon_hours", nullable = false)
  private Integer interestHorizonHours;

  @Column(name = "momentum_lookback_hours", nullable = false)
  private Integer momentumLookbackHours;

  @Column(name = "updated_by")
  private String updatedBy;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  public Long getId() {
    return id;
  }

  public BigDecimal getHypeSensitivity() {
    return hypeSensitivity;
  }

  public Integer getInterestHorizonHours() {
    return interestHorizonHours;
  }

  public Integer getMomentumLookbackHours() {
    return momentumLookbackHours;
  }

  public void setHypeSensitivity(BigDecimal hypeSensitivity) {
    this.hypeSensitivity = hypeSensitivity;
  }

  public void setInterestHorizonHours(Integer interestHorizonHours) {
    this.interestHorizonHours = interestHorizonHours;
  }

  public void setMomentumLookbackHours(Integer momentumLookbackHours) {
    this.momentumLookbackHours = momentumLookbackHours;
  }

  public String getUpdatedBy() {
    return updatedBy;
  }

  public void setUpdatedBy(String updatedBy) {
    this.updatedBy = updatedBy;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }

  public void setUpdatedAt(Instant updatedAt) {
    this.updatedAt = updatedAt;
  }
}


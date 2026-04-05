package com.goblinbank.config;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "global_interest_config")
public class GlobalInterestConfig {
  @Id private Long id;

  @Column(name = "base_rate_per_hour", nullable = false, precision = 12, scale = 8)
  private BigDecimal baseRatePerHour;

  @Column(name = "updated_by")
  private String updatedBy;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  public Long getId() {
    return id;
  }

  public BigDecimal getBaseRatePerHour() {
    return baseRatePerHour;
  }

  public void setBaseRatePerHour(BigDecimal baseRatePerHour) {
    this.baseRatePerHour = baseRatePerHour;
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


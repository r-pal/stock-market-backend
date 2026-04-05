package com.goblinbank.config;

import java.math.BigDecimal;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "goblin.rates")
public class RateBoundsProperties {

  private BigDecimal minHourly = new BigDecimal("-1");
  private BigDecimal maxHourly = new BigDecimal("1");

  public BigDecimal getMinHourly() {
    return minHourly;
  }

  public void setMinHourly(BigDecimal minHourly) {
    this.minHourly = minHourly;
  }

  public BigDecimal getMaxHourly() {
    return maxHourly;
  }

  public void setMaxHourly(BigDecimal maxHourly) {
    this.maxHourly = maxHourly;
  }
}

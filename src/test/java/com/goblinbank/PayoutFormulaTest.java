package com.goblinbank;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import org.junit.jupiter.api.Test;

class PayoutFormulaTest {

  private static final MathContext MC = new MathContext(24, RoundingMode.HALF_UP);

  @Test
  void sellPayoutMatchesSharePriceRatio() {
    BigDecimal principal = new BigDecimal("5000");
    BigDecimal priceBuy = new BigDecimal("10.00");
    BigDecimal priceSell = new BigDecimal("20.00");
    BigDecimal payout = principal.multiply(priceSell, MC).divide(priceBuy, MC);
    assertEquals(0, payout.compareTo(new BigDecimal("10000")));
  }
}

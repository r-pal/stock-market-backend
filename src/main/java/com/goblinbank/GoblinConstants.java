package com.goblinbank;

import java.math.BigDecimal;

public final class GoblinConstants {

  private GoblinConstants() {}

  public static final int SHARES_OUTSTANDING = 1000;
  public static final BigDecimal WEALTH_MOMENTUM_EPS = new BigDecimal("1.00");
  public static final BigDecimal TICKER_PRICE_EPS = new BigDecimal("0.01");
  public static final BigDecimal INVESTMENT_PRICE_EPS = new BigDecimal("0.000001");
  public static final String CURRENCY_SYMBOL = "₲";

  public static final String ROLE_BANKER = "BANKER";
  public static final String ROLE_HOUSE_PORTAL = "HOUSE_PORTAL";

  public static final String POSITION_OPEN = "OPEN";
  public static final String POSITION_CLOSED = "CLOSED";
}

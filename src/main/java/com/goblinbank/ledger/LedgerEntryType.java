package com.goblinbank.ledger;

public final class LedgerEntryType {

  private LedgerEntryType() {}

  public static final String DEPOSIT = "DEPOSIT";
  public static final String WITHDRAW = "WITHDRAW";
  public static final String INTEREST_ACCRUAL = "INTEREST_ACCRUAL";
  public static final String INVESTMENT_BUY = "INVESTMENT_BUY";
  public static final String INVESTMENT_SELL = "INVESTMENT_SELL";
}

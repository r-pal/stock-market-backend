package com.goblinbank.ledger;

import com.goblinbank.account.HouseAccount;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "ledger_entry")
public class LedgerEntry {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  @JoinColumn(name = "account_id", nullable = false)
  private HouseAccount account;

  @Column(name = "entry_type", nullable = false)
  private String entryType;

  @Column(name = "amount", nullable = false, precision = 19, scale = 2)
  private BigDecimal amount;

  @Column(name = "before_balance", nullable = false, precision = 19, scale = 2)
  private BigDecimal beforeBalance;

  @Column(name = "after_balance", nullable = false, precision = 19, scale = 2)
  private BigDecimal afterBalance;

  @Column(name = "performed_by")
  private String performedBy;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  public Long getId() {
    return id;
  }

  public HouseAccount getAccount() {
    return account;
  }

  public String getEntryType() {
    return entryType;
  }

  public BigDecimal getAmount() {
    return amount;
  }

  public BigDecimal getBeforeBalance() {
    return beforeBalance;
  }

  public BigDecimal getAfterBalance() {
    return afterBalance;
  }

  public String getPerformedBy() {
    return performedBy;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public void setAccount(HouseAccount account) {
    this.account = account;
  }

  public void setEntryType(String entryType) {
    this.entryType = entryType;
  }

  public void setAmount(BigDecimal amount) {
    this.amount = amount;
  }

  public void setBeforeBalance(BigDecimal beforeBalance) {
    this.beforeBalance = beforeBalance;
  }

  public void setAfterBalance(BigDecimal afterBalance) {
    this.afterBalance = afterBalance;
  }

  public void setPerformedBy(String performedBy) {
    this.performedBy = performedBy;
  }

  public void setCreatedAt(Instant createdAt) {
    this.createdAt = createdAt;
  }
}


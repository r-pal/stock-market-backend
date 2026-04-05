package com.goblinbank.account;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "house_account")
public class HouseAccount {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "house_name", nullable = false)
  private String houseName;

  @Column(name = "balance", nullable = false, precision = 19, scale = 2)
  private BigDecimal balance;

  @Column(name = "account_rate_adjustment_per_hour", nullable = false, precision = 12, scale = 8)
  private BigDecimal accountRateAdjustmentPerHour;

  @Column(name = "portal_password_hash")
  private String portalPasswordHash;

  @Column(name = "portal_password_updated_at")
  private Instant portalPasswordUpdatedAt;

  @Column(name = "deleted_at")
  private Instant deletedAt;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  @PrePersist
  void prePersist() {
    Instant now = Instant.now();
    if (createdAt == null) createdAt = now;
    updatedAt = now;
  }

  @PreUpdate
  void preUpdate() {
    updatedAt = Instant.now();
  }

  public void setUpdatedAt(Instant updatedAt) {
    this.updatedAt = updatedAt;
  }

  public Long getId() {
    return id;
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

  public BigDecimal getAccountRateAdjustmentPerHour() {
    return accountRateAdjustmentPerHour;
  }

  public void setAccountRateAdjustmentPerHour(BigDecimal accountRateAdjustmentPerHour) {
    this.accountRateAdjustmentPerHour = accountRateAdjustmentPerHour;
  }

  public String getPortalPasswordHash() {
    return portalPasswordHash;
  }

  public void setPortalPasswordHash(String portalPasswordHash) {
    this.portalPasswordHash = portalPasswordHash;
  }

  public Instant getPortalPasswordUpdatedAt() {
    return portalPasswordUpdatedAt;
  }

  public void setPortalPasswordUpdatedAt(Instant portalPasswordUpdatedAt) {
    this.portalPasswordUpdatedAt = portalPasswordUpdatedAt;
  }

  public Instant getDeletedAt() {
    return deletedAt;
  }

  public void setDeletedAt(Instant deletedAt) {
    this.deletedAt = deletedAt;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }
}


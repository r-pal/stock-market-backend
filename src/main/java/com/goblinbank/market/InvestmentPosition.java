package com.goblinbank.market;

import com.goblinbank.account.HouseAccount;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "investment_position")
public class InvestmentPosition {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  @JoinColumn(name = "buyer_house_id", nullable = false)
  private HouseAccount buyerHouse;

  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  @JoinColumn(name = "target_house_id", nullable = false)
  private HouseAccount targetHouse;

  @Column(name = "principal_amount", nullable = false, precision = 19, scale = 2)
  private BigDecimal principalAmount;

  @Column(name = "target_wealth_at_buy", nullable = false, precision = 19, scale = 2)
  private BigDecimal targetWealthAtBuy;

  @Column(name = "target_share_price_at_buy", nullable = false, precision = 19, scale = 6)
  private BigDecimal targetSharePriceAtBuy;

  @Column(name = "target_share_price_at_sell", precision = 19, scale = 6)
  private BigDecimal targetSharePriceAtSell;

  @Column(name = "payout_amount", precision = 19, scale = 2)
  private BigDecimal payoutAmount;

  @Column(name = "status", nullable = false)
  private String status;

  @Column(name = "bought_at", nullable = false)
  private Instant boughtAt;

  @Column(name = "sold_at")
  private Instant soldAt;

  public Long getId() {
    return id;
  }

  public HouseAccount getBuyerHouse() {
    return buyerHouse;
  }

  public void setBuyerHouse(HouseAccount buyerHouse) {
    this.buyerHouse = buyerHouse;
  }

  public HouseAccount getTargetHouse() {
    return targetHouse;
  }

  public void setTargetHouse(HouseAccount targetHouse) {
    this.targetHouse = targetHouse;
  }

  public BigDecimal getPrincipalAmount() {
    return principalAmount;
  }

  public void setPrincipalAmount(BigDecimal principalAmount) {
    this.principalAmount = principalAmount;
  }

  public BigDecimal getTargetWealthAtBuy() {
    return targetWealthAtBuy;
  }

  public void setTargetWealthAtBuy(BigDecimal targetWealthAtBuy) {
    this.targetWealthAtBuy = targetWealthAtBuy;
  }

  public BigDecimal getTargetSharePriceAtBuy() {
    return targetSharePriceAtBuy;
  }

  public void setTargetSharePriceAtBuy(BigDecimal targetSharePriceAtBuy) {
    this.targetSharePriceAtBuy = targetSharePriceAtBuy;
  }

  public BigDecimal getTargetSharePriceAtSell() {
    return targetSharePriceAtSell;
  }

  public void setTargetSharePriceAtSell(BigDecimal targetSharePriceAtSell) {
    this.targetSharePriceAtSell = targetSharePriceAtSell;
  }

  public BigDecimal getPayoutAmount() {
    return payoutAmount;
  }

  public void setPayoutAmount(BigDecimal payoutAmount) {
    this.payoutAmount = payoutAmount;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public Instant getBoughtAt() {
    return boughtAt;
  }

  public void setBoughtAt(Instant boughtAt) {
    this.boughtAt = boughtAt;
  }

  public Instant getSoldAt() {
    return soldAt;
  }

  public void setSoldAt(Instant soldAt) {
    this.soldAt = soldAt;
  }
}


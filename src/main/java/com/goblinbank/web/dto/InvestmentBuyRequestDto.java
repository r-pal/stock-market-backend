package com.goblinbank.web.dto;

import java.math.BigDecimal;

public record InvestmentBuyRequestDto(
    Long targetHouseId, BigDecimal amount, Long buyerHouseId, Long stockId) {}

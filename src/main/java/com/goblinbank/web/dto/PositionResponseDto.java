package com.goblinbank.web.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record PositionResponseDto(
    Long id,
    Long buyerHouseId,
    Long targetHouseId,
    Long stockId,
    String stockType,
    String stockDisplayName,
    BigDecimal principalAmount,
    String status,
    Instant boughtAt,
    Instant soldAt,
    BigDecimal payoutAmount) {}

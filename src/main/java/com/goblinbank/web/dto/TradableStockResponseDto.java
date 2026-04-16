package com.goblinbank.web.dto;

import java.math.BigDecimal;

public record TradableStockResponseDto(
    Long id,
    String displayName,
    String stockType,
    Long houseAccountId,
    BigDecimal currentPrice,
    boolean active) {}


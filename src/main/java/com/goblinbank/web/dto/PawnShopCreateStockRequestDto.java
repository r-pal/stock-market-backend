package com.goblinbank.web.dto;

import java.math.BigDecimal;

public record PawnShopCreateStockRequestDto(String displayName, BigDecimal currentPrice) {}


package com.goblinbank.web.dto;

import java.math.BigDecimal;

public record SharePriceConfigPatchDto(
    BigDecimal hypeSensitivity,
    Integer interestHorizonHours,
    Integer momentumLookbackHours) {}

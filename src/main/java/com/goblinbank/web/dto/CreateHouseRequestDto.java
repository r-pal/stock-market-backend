package com.goblinbank.web.dto;

import java.math.BigDecimal;

public record CreateHouseRequestDto(String name, BigDecimal initialBalance) {}

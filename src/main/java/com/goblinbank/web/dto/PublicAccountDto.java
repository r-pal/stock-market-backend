package com.goblinbank.web.dto;

public record PublicAccountDto(
    Long id,
    String houseName,
    String balance,
    String accountRateAdjustmentPerHour,
    String effectiveRatePerHour,
    String sharePrice,
    String holdingsValue,
    String momentum) {}

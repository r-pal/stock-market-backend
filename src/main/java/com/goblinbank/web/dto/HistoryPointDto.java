package com.goblinbank.web.dto;

public record HistoryPointDto(
    int minutes, String balance, String sharePrice, String effectiveRatePerHour) {}

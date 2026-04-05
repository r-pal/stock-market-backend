package com.goblinbank.web.dto;

import java.util.List;

public record HistoryResponseDto(
    int intervalMinutes,
    int gameMinutesTotal,
    int gameMinutesElapsed,
    int gameMinutesRemaining,
    String currencySymbol,
    List<HistorySeriesDto> series) {}

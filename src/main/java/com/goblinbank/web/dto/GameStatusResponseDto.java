package com.goblinbank.web.dto;

import java.time.Instant;

public record GameStatusResponseDto(
    Instant gameStartAt,
    int gameDurationMinutes,
    int gameMinutesElapsed,
    int gameMinutesRemaining) {}

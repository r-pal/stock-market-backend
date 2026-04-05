package com.goblinbank.web.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record LedgerLineResponseDto(
    long id,
    long accountId,
    String entryType,
    BigDecimal amount,
    BigDecimal beforeBalance,
    BigDecimal afterBalance,
    String performedBy,
    Instant createdAt) {}

package com.goblinbank.web.dto;

import java.util.List;

public record PublicAccountsResponseDto(String baseRatePerHour, List<PublicAccountDto> accounts) {}

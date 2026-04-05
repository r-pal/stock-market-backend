package com.goblinbank.market;

import com.goblinbank.web.dto.PositionResponseDto;

public final class InvestmentDtoMapper {

  private InvestmentDtoMapper() {}

  public static PositionResponseDto toDto(InvestmentPosition p) {
    return new PositionResponseDto(
        p.getId(),
        p.getBuyerHouse().getId(),
        p.getTargetHouse().getId(),
        p.getPrincipalAmount(),
        p.getStatus(),
        p.getBoughtAt(),
        p.getSoldAt(),
        p.getPayoutAmount());
  }
}

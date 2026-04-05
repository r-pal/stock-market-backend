package com.goblinbank.house;

import com.goblinbank.account.PublicAccountQueryService;
import com.goblinbank.market.InvestmentDtoMapper;
import com.goblinbank.market.InvestmentPosition;
import com.goblinbank.market.InvestmentPositionRepository;
import com.goblinbank.market.InvestmentService;
import com.goblinbank.web.dto.InvestmentBuyRequestDto;
import com.goblinbank.web.dto.PositionResponseDto;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import java.time.Instant;
import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/house")
@SecurityRequirement(name = "bearerAuth")
public class HouseStockController {

  private final PublicAccountQueryService publicAccounts;
  private final InvestmentService investmentService;
  private final InvestmentPositionRepository positionRepo;

  public HouseStockController(
      PublicAccountQueryService publicAccounts,
      InvestmentService investmentService,
      InvestmentPositionRepository positionRepo) {
    this.publicAccounts = publicAccounts;
    this.investmentService = investmentService;
    this.positionRepo = positionRepo;
  }

  @GetMapping("/account")
  public com.goblinbank.web.dto.PublicAccountDto account(Authentication auth) {
    long houseId = (Long) auth.getPrincipal();
    return publicAccounts.one(houseId, Instant.now());
  }

  @PostMapping("/investments/buy")
  public PositionResponseDto buy(@RequestBody InvestmentBuyRequestDto body, Authentication auth) {
    long buyer = (Long) auth.getPrincipal();
    InvestmentPosition p =
        investmentService.buy(buyer, body.targetHouseId(), body.amount(), "house:" + buyer);
    return InvestmentDtoMapper.toDto(p);
  }

  @PostMapping("/investments/{positionId}/sell")
  public PositionResponseDto sell(@PathVariable Long positionId, Authentication auth) {
    long buyer = (Long) auth.getPrincipal();
    InvestmentPosition p = investmentService.sell(positionId, buyer, "house:" + buyer);
    return InvestmentDtoMapper.toDto(p);
  }

  @GetMapping("/investments")
  public List<PositionResponseDto> list(
      @RequestParam(required = false) String status, Authentication auth) {
    long buyer = (Long) auth.getPrincipal();
    List<InvestmentPosition> rows =
        status == null || status.isBlank()
            ? positionRepo.findByBuyerHouseIdFetched(buyer)
            : positionRepo.findByBuyerHouseIdAndStatusFetched(buyer, status);
    return rows.stream().map(InvestmentDtoMapper::toDto).toList();
  }
}

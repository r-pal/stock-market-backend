package com.goblinbank.account;

import com.goblinbank.GoblinConstants;
import com.goblinbank.config.GlobalInterestConfigRepository;
import com.goblinbank.market.InvestmentPositionRepository;
import com.goblinbank.shareprice.SharePriceService;
import com.goblinbank.web.dto.PublicAccountDto;
import com.goblinbank.web.dto.PublicAccountsResponseDto;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PublicAccountQueryService {

  private final HouseAccountRepository accountRepo;
  private final GlobalInterestConfigRepository globalInterestRepo;
  private final InvestmentPositionRepository positionRepo;
  private final SharePriceService sharePriceService;

  public PublicAccountQueryService(
      HouseAccountRepository accountRepo,
      GlobalInterestConfigRepository globalInterestRepo,
      InvestmentPositionRepository positionRepo,
      SharePriceService sharePriceService) {
    this.accountRepo = accountRepo;
    this.globalInterestRepo = globalInterestRepo;
    this.positionRepo = positionRepo;
    this.sharePriceService = sharePriceService;
  }

  @Transactional(readOnly = true)
  public PublicAccountsResponseDto listAll(Instant now) {
    var base = globalInterestRepo.findById(1L).orElseThrow().getBaseRatePerHour();
    List<HouseAccount> houses = accountRepo.findAllActive();
    List<PublicAccountDto> dtos = houses.stream().map(h -> map(h, base, now)).toList();
    return new PublicAccountsResponseDto(base.toPlainString(), dtos);
  }

  @Transactional(readOnly = true)
  public PublicAccountDto one(Long id, Instant now) {
    HouseAccount h =
        accountRepo
            .findActiveById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found"));
    var base = globalInterestRepo.findById(1L).orElseThrow().getBaseRatePerHour();
    return map(h, base, now);
  }

  private PublicAccountDto map(HouseAccount h, java.math.BigDecimal baseRate, Instant now) {
    var open =
        positionRepo.findOpenForBuyerFetched(h.getId(), GoblinConstants.POSITION_OPEN);
    var eff = sharePriceService.effectiveRate(h);
    var holdings = sharePriceService.holdingsValue(h, open);
    var mom = sharePriceService.momentum(h, now);
    var price = sharePriceService.sharePrice(h, open, now);
    return new PublicAccountDto(
        h.getId(),
        h.getHouseName(),
        h.getBalance().setScale(2, RoundingMode.HALF_UP).toPlainString(),
        h.getAccountRateAdjustmentPerHour().toPlainString(),
        eff.toPlainString(),
        price.toPlainString(),
        holdings.toPlainString(),
        mom.setScale(6, RoundingMode.HALF_UP).toPlainString());
  }
}

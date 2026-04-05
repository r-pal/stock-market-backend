package com.goblinbank.account;

import com.goblinbank.web.dto.PublicAccountDto;
import com.goblinbank.web.dto.PublicAccountsResponseDto;
import java.time.Instant;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/public")
public class PublicAccountController {

  private final PublicAccountQueryService queryService;

  public PublicAccountController(PublicAccountQueryService queryService) {
    this.queryService = queryService;
  }

  @GetMapping("/accounts")
  public PublicAccountsResponseDto accounts() {
    return queryService.listAll(Instant.now());
  }

  @GetMapping("/accounts/{id}")
  public PublicAccountDto account(@PathVariable Long id) {
    return queryService.one(id, Instant.now());
  }
}

package com.goblinbank.wealth;

import com.goblinbank.web.dto.HistoryResponseDto;
import java.time.Instant;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/public/history")
public class WealthHistoryController {

  private final WealthHistoryQueryService queryService;

  public WealthHistoryController(WealthHistoryQueryService queryService) {
    this.queryService = queryService;
  }

  @GetMapping
  public HistoryResponseDto history(
      @RequestParam(required = false) Integer fromMinutes,
      @RequestParam(required = false) Integer toMinutes,
      @RequestParam(required = false) List<Long> houseIds) {
    return queryService.buildHistory(fromMinutes, toMinutes, houseIds, Instant.now());
  }
}

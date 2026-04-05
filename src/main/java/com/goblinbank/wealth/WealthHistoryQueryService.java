package com.goblinbank.wealth;

import com.goblinbank.GoblinConstants;
import com.goblinbank.account.HouseAccount;
import com.goblinbank.account.HouseAccountRepository;
import com.goblinbank.game.GameClockConfig;
import com.goblinbank.game.GameClockConfigRepository;
import com.goblinbank.game.GameClockService;
import com.goblinbank.game.HistoryConfigRepository;
import com.goblinbank.web.dto.HistoryPointDto;
import com.goblinbank.web.dto.HistoryResponseDto;
import com.goblinbank.web.dto.HistorySeriesDto;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WealthHistoryQueryService {

  private final GameClockConfigRepository gameClockRepo;
  private final GameClockService gameClock;
  private final HistoryConfigRepository historyConfigRepo;
  private final HouseAccountRepository accountRepo;
  private final HouseWealthSnapshotRepository snapshotRepo;

  public WealthHistoryQueryService(
      GameClockConfigRepository gameClockRepo,
      GameClockService gameClock,
      HistoryConfigRepository historyConfigRepo,
      HouseAccountRepository accountRepo,
      HouseWealthSnapshotRepository snapshotRepo) {
    this.gameClockRepo = gameClockRepo;
    this.gameClock = gameClock;
    this.historyConfigRepo = historyConfigRepo;
    this.accountRepo = accountRepo;
    this.snapshotRepo = snapshotRepo;
  }

  @Transactional(readOnly = true)
  public HistoryResponseDto buildHistory(
      Integer fromMinutes, Integer toMinutes, List<Long> houseIds, Instant now) {
    GameClockConfig g = gameClockRepo.findById(1L).orElseThrow();
    int interval = historyConfigRepo.findById(1L).orElseThrow().getSnapshotIntervalMinutes();
    int total = g.getGameDurationMinutes();
    int elapsed = gameClock.currentElapsedMinutes(now);
    int remaining = Math.max(0, total - elapsed);

    int from = fromMinutes == null ? 0 : clamp(fromMinutes, 0, total);
    int to = toMinutes == null ? total : clamp(toMinutes, 0, total);
    if (from > to) {
      throw new IllegalArgumentException("fromMinutes must be <= toMinutes");
    }

    List<Long> ids;
    if (houseIds == null || houseIds.isEmpty()) {
      ids = accountRepo.findAllActive().stream().map(HouseAccount::getId).toList();
    } else {
      ids = houseIds;
    }

    List<HouseWealthSnapshot> rows = snapshotRepo.findForHistoryChart(ids, from, to);
    Map<Long, HistorySeriesDto> byHouse = new LinkedHashMap<>();
    for (Long id : ids) {
      HouseAccount ha = accountRepo.findActiveById(id).orElse(null);
      if (ha == null) {
        continue;
      }
      byHouse.put(id, new HistorySeriesDto(id, ha.getHouseName(), new ArrayList<>()));
    }
    for (HouseWealthSnapshot s : rows) {
      long hid = s.getHouse().getId();
      HistorySeriesDto series = byHouse.get(hid);
      if (series == null) {
        continue;
      }
      series.points()
          .add(
              new HistoryPointDto(
                  s.getMinutes(),
                  s.getBalance().toPlainString(),
                  s.getSharePrice().toPlainString(),
                  s.getEffectiveRatePerHour().toPlainString()));
    }

    return new HistoryResponseDto(
        interval,
        total,
        g.getGameStartAt() == null ? 0 : elapsed,
        g.getGameStartAt() == null ? total : remaining,
        GoblinConstants.CURRENCY_SYMBOL,
        new ArrayList<>(byHouse.values()));
  }

  private static int clamp(int v, int min, int max) {
    return Math.max(min, Math.min(max, v));
  }
}

package com.goblinbank.game;

import com.goblinbank.ticker.TickerBaselineService;
import java.time.Instant;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GameAdminService {

  private final GameClockConfigRepository gameRepo;
  private final TickerBaselineService tickerBaselineService;

  public GameAdminService(
      GameClockConfigRepository gameRepo, TickerBaselineService tickerBaselineService) {
    this.gameRepo = gameRepo;
    this.tickerBaselineService = tickerBaselineService;
  }

  @Transactional(readOnly = true)
  public GameClockConfig getConfig() {
    return gameRepo.findById(1L).orElseThrow();
  }

  @Transactional
  public GameClockConfig setDurationMinutes(int minutes, String bankerUser) {
    GameClockConfig g = gameRepo.findById(1L).orElseThrow();
    if (g.getGameStartAt() != null) {
      throw new IllegalStateException("Game already started; duration is locked");
    }
    if (minutes <= 0 || minutes > 100_000) {
      throw new IllegalArgumentException("Invalid game duration");
    }
    g.setGameDurationMinutes(minutes);
    return gameRepo.save(g);
  }

  /** Idempotent: if already started, returns existing config unchanged. */
  @Transactional
  public GameClockConfig startGame(String bankerUser) {
    GameClockConfig g = gameRepo.findById(1L).orElseThrow();
    if (g.getGameStartAt() != null) {
      return g;
    }
    g.setGameStartAt(Instant.now());
    gameRepo.save(g);
    tickerBaselineService.syncAllSharePrices(Instant.now());
    return g;
  }
}

package com.goblinbank.game;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GameClockService {

  private final GameClockConfigRepository repo;

  public GameClockService(GameClockConfigRepository repo) {
    this.repo = repo;
  }

  @Transactional(readOnly = true)
  public GameClockConfig get() {
    return repo.findById(1L).orElseThrow(() -> new IllegalStateException("game_clock_config missing"));
  }

  /** Minutes elapsed since game start, clamped to [0, duration]. 0 if not started. */
  public int currentElapsedMinutes(Instant now) {
    GameClockConfig g = get();
    if (g.getGameStartAt() == null) {
      return 0;
    }
    long seconds = ChronoUnit.SECONDS.between(g.getGameStartAt(), now);
    if (seconds < 0) {
      return 0;
    }
    int m = (int) (seconds / 60);
    return Math.min(m, g.getGameDurationMinutes());
  }

  public int minutesRemaining(Instant now) {
    GameClockConfig g = get();
    return g.getGameDurationMinutes() - currentElapsedMinutes(now);
  }

  public boolean isStarted() {
    return get().getGameStartAt() != null;
  }

  public Instant instantForGameMinute(int minutesElapsed, GameClockConfig g) {
    if (g.getGameStartAt() == null) {
      return null;
    }
    return g.getGameStartAt().plus(minutesElapsed, ChronoUnit.MINUTES);
  }
}

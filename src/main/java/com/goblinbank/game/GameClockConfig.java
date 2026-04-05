package com.goblinbank.game;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "game_clock_config")
public class GameClockConfig {
  @Id private Long id;

  @Column(name = "game_start_at")
  private Instant gameStartAt;

  @Column(name = "game_duration_minutes", nullable = false)
  private Integer gameDurationMinutes;

  public Long getId() {
    return id;
  }

  public Instant getGameStartAt() {
    return gameStartAt;
  }

  public Integer getGameDurationMinutes() {
    return gameDurationMinutes;
  }

  public void setGameStartAt(Instant gameStartAt) {
    this.gameStartAt = gameStartAt;
  }

  public void setGameDurationMinutes(Integer gameDurationMinutes) {
    this.gameDurationMinutes = gameDurationMinutes;
  }
}


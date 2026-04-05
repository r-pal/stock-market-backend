package com.goblinbank.game;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "history_config")
public class HistoryConfig {
  @Id private Long id;

  @Column(name = "snapshot_interval_minutes", nullable = false)
  private Integer snapshotIntervalMinutes;

  @Column(name = "updated_by")
  private String updatedBy;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  public Long getId() {
    return id;
  }

  public Integer getSnapshotIntervalMinutes() {
    return snapshotIntervalMinutes;
  }

  public void setSnapshotIntervalMinutes(Integer snapshotIntervalMinutes) {
    this.snapshotIntervalMinutes = snapshotIntervalMinutes;
  }

  public String getUpdatedBy() {
    return updatedBy;
  }

  public void setUpdatedBy(String updatedBy) {
    this.updatedBy = updatedBy;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }

  public void setUpdatedAt(Instant updatedAt) {
    this.updatedAt = updatedAt;
  }
}


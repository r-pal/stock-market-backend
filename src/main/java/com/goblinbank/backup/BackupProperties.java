package com.goblinbank.backup;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "goblin.backup")
public class BackupProperties {
  /** Enable/disable automated backups. */
  private boolean enabled = true;

  /** How often to write a backup (seconds). */
  private int intervalSeconds = 300;

  /** Directory (relative to working dir) where backups are written. */
  private String dir = "backups";

  /** Number of backups to retain (oldest are pruned). */
  private int retain = 50;

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  public int getIntervalSeconds() {
    return intervalSeconds;
  }

  public void setIntervalSeconds(int intervalSeconds) {
    this.intervalSeconds = intervalSeconds;
  }

  public String getDir() {
    return dir;
  }

  public void setDir(String dir) {
    this.dir = dir;
  }

  public int getRetain() {
    return retain;
  }

  public void setRetain(int retain) {
    this.retain = retain;
  }
}


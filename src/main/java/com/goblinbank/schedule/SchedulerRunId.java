package com.goblinbank.schedule;

import java.io.Serializable;
import java.util.Objects;

public class SchedulerRunId implements Serializable {
  private String jobName;
  private String bucketKey;

  public SchedulerRunId() {}

  public SchedulerRunId(String jobName, String bucketKey) {
    this.jobName = jobName;
    this.bucketKey = bucketKey;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    SchedulerRunId that = (SchedulerRunId) o;
    return Objects.equals(jobName, that.jobName) && Objects.equals(bucketKey, that.bucketKey);
  }

  @Override
  public int hashCode() {
    return Objects.hash(jobName, bucketKey);
  }
}

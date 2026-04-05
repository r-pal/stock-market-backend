package com.goblinbank.schedule;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "scheduler_run")
@IdClass(SchedulerRunId.class)
public class SchedulerRun {

  @Id
  @Column(name = "job_name", nullable = false)
  private String jobName;

  @Id
  @Column(name = "bucket_key", nullable = false)
  private String bucketKey;

  @Column(name = "run_at", nullable = false)
  private Instant runAt;

  public String getJobName() {
    return jobName;
  }

  public void setJobName(String jobName) {
    this.jobName = jobName;
  }

  public String getBucketKey() {
    return bucketKey;
  }

  public void setBucketKey(String bucketKey) {
    this.bucketKey = bucketKey;
  }

  public Instant getRunAt() {
    return runAt;
  }

  public void setRunAt(Instant runAt) {
    this.runAt = runAt;
  }
}

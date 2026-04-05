package com.goblinbank.wealth;

import com.goblinbank.schedule.SchedulerRun;
import com.goblinbank.schedule.SchedulerRunId;
import com.goblinbank.schedule.SchedulerRunRepository;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class WealthSnapshotScheduler {

  private static final String JOB = "WEALTH_SNAPSHOT";
  private static final DateTimeFormatter BUCKET =
      DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm'Z'").withZone(ZoneOffset.UTC);

  private final WealthSnapshotService wealthSnapshotService;
  private final SchedulerRunRepository schedulerRunRepo;

  public WealthSnapshotScheduler(
      WealthSnapshotService wealthSnapshotService,
      SchedulerRunRepository schedulerRunRepo) {
    this.wealthSnapshotService = wealthSnapshotService;
    this.schedulerRunRepo = schedulerRunRepo;
  }

  @Scheduled(fixedDelay = 60000, initialDelay = 15000)
  public void tick() {
    Instant now = Instant.now();
    if (!wealthSnapshotService.shouldCaptureThisMinute(now)) {
      return;
    }
    ZonedDateTime z = now.atZone(ZoneOffset.UTC).truncatedTo(ChronoUnit.MINUTES);
    String bucketKey = BUCKET.format(z.toInstant());
    synchronized (("goblin-" + JOB + "-" + bucketKey).intern()) {
      if (schedulerRunRepo.existsById(new SchedulerRunId(JOB, bucketKey))) {
        return;
      }
      wealthSnapshotService.captureAlignedMinute(z.toInstant());
      SchedulerRun run = new SchedulerRun();
      run.setJobName(JOB);
      run.setBucketKey(bucketKey);
      run.setRunAt(now);
      schedulerRunRepo.save(run);
    }
  }
}

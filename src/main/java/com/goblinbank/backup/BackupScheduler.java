package com.goblinbank.backup;

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
public class BackupScheduler {

  private static final String JOB = "DATA_BACKUP";
  private static final DateTimeFormatter BUCKET =
      DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm'Z'").withZone(ZoneOffset.UTC);

  private final BackupProperties props;
  private final BackupService backupService;
  private final SchedulerRunRepository schedulerRunRepo;

  public BackupScheduler(
      BackupProperties props, BackupService backupService, SchedulerRunRepository schedulerRunRepo) {
    this.props = props;
    this.backupService = backupService;
    this.schedulerRunRepo = schedulerRunRepo;
  }

  @Scheduled(fixedDelayString = "#{@backupProperties.intervalSeconds * 1000}", initialDelay = 20000)
  public void tick() {
    if (!props.isEnabled()) {
      return;
    }
    Instant now = Instant.now();
    // Bucket at minute resolution so restarts don't spam duplicates.
    ZonedDateTime z = now.atZone(ZoneOffset.UTC).truncatedTo(ChronoUnit.MINUTES);
    String bucketKey = BUCKET.format(z.toInstant());
    synchronized (("goblin-" + JOB + "-" + bucketKey).intern()) {
      if (schedulerRunRepo.existsById(new SchedulerRunId(JOB, bucketKey))) {
        return;
      }
      backupService.writeBackupNow("scheduler");
      SchedulerRun run = new SchedulerRun();
      run.setJobName(JOB);
      run.setBucketKey(bucketKey);
      run.setRunAt(now);
      schedulerRunRepo.save(run);
    }
  }
}


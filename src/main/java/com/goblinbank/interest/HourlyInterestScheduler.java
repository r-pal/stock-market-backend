package com.goblinbank.interest;

import com.goblinbank.schedule.SchedulerRun;
import com.goblinbank.schedule.SchedulerRunId;
import com.goblinbank.schedule.SchedulerRunRepository;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class HourlyInterestScheduler {

  private static final String JOB = "HOURLY_INTEREST";
  private static final DateTimeFormatter BUCKET =
      DateTimeFormatter.ofPattern("yyyy-MM-dd-HH").withZone(ZoneOffset.UTC);

  private final SchedulerRunRepository schedulerRunRepo;
  private final InterestService interestService;

  public HourlyInterestScheduler(
      SchedulerRunRepository schedulerRunRepo, InterestService interestService) {
    this.schedulerRunRepo = schedulerRunRepo;
    this.interestService = interestService;
  }

  @Scheduled(cron = "0 0 * * * *", zone = "UTC")
  public void runHourly() {
    Instant now = Instant.now();
    String bucket = BUCKET.format(now);
    synchronized (("goblin-" + JOB + "-" + bucket).intern()) {
      if (schedulerRunRepo.existsById(new SchedulerRunId(JOB, bucket))) {
        return;
      }
      interestService.accrueHour(now);
      SchedulerRun run = new SchedulerRun();
      run.setJobName(JOB);
      run.setBucketKey(bucket);
      run.setRunAt(now);
      schedulerRunRepo.save(run);
    }
  }
}

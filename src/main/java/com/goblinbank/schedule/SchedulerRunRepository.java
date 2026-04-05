package com.goblinbank.schedule;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SchedulerRunRepository extends JpaRepository<SchedulerRun, SchedulerRunId> {}

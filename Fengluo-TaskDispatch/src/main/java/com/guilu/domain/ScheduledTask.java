package com.guilu.domain;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.springframework.scheduling.TaskScheduler;

import java.util.concurrent.ScheduledFuture;

@EqualsAndHashCode(callSuper = true)
public class ScheduledTask extends Task {
    @Getter
    private final ScheduledFuture<?> future;
    @Getter
    private final TaskScheduler scheduler;

    public ScheduledTask(String taskId, ScheduledFuture<?> future, TaskScheduler scheduler) {
        super(taskId);
        this.future = future;
        this.scheduler = scheduler;
    }
}

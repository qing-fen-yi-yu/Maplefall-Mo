package com.guilu.domain;

import lombok.EqualsAndHashCode;
import lombok.Getter;

@EqualsAndHashCode(callSuper = true)
public class DispatchScheduledTask extends Task{
    @Getter
    private final org.springframework.scheduling.config.ScheduledTask task;
    public DispatchScheduledTask(org.springframework.scheduling.config.ScheduledTask task, String taskId) {
        super(taskId);
        this.task = task;
    }

    @Override
    public String toString() {
        return "ScheduledTask{" +
                "task=" + task +
                '}';
    }
}

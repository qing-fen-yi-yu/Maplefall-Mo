package com.guilu.domain;

import lombok.Data;

@Data
public abstract class Task {
    private String taskId;

    protected Task(String taskId) {
        if (taskId == null || taskId.isBlank()) {
            throw new IllegalArgumentException("taskId must not be blank");
        }
        this.taskId = taskId;
    }
}

package com.guilu.domain;

import lombok.Data;

@Data
public class Task{
    private String taskId;

    public Task(String taskId){
        this.taskId = taskId;
    }
}

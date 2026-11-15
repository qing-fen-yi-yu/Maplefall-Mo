package com.guilu.domain;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DefaultPoolSnapshot {
    private String name;
    private Integer coreSize;
    private Integer maxSize;
    private Integer poolSize;
    private Integer activeCount;
    private Integer queueSize;
    private Integer remainingCapacity;
    private Long completedTasks;
    private String rejectHandler;
    private Long avgTime;
    private Long totalTime;
    private Long numTasks;

    public DefaultPoolSnapshot(String name, Integer coreSize, Integer maxSize, Integer poolSize, Integer activeCount, Integer queueSize, Integer remainingCapacity, Long completedTasks,String rejectHandler,Long avgTime,Long totalTime,Long numTasks) {
        this.name = name;
        this.coreSize = coreSize;
        this.maxSize = maxSize;
        this.poolSize = poolSize;
        this.activeCount = activeCount;
        this.queueSize = queueSize;
        this.remainingCapacity = remainingCapacity;
        this.completedTasks = completedTasks;
        this.rejectHandler = rejectHandler;
        this.avgTime = avgTime;
        this.totalTime = totalTime;
        this.numTasks = numTasks;
    }

}

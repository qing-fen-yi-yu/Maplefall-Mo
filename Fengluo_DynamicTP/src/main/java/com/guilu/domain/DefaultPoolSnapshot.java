package com.guilu.domain;


import java.util.concurrent.RejectedExecutionHandler;

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

    public DefaultPoolSnapshot(String name, Integer coreSize, Integer maxSize, Integer poolSize, Integer activeCount, Integer queueSize, Integer remainingCapacity, Long completedTasks,String rejectHandler) {
        this.name = name;
        this.coreSize = coreSize;
        this.maxSize = maxSize;
        this.poolSize = poolSize;
        this.activeCount = activeCount;
        this.queueSize = queueSize;
        this.remainingCapacity = remainingCapacity;
        this.completedTasks = completedTasks;
        this.rejectHandler = rejectHandler;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getCoreSize() {
        return coreSize;
    }

    public void setCoreSize(Integer coreSize) {
        this.coreSize = coreSize;
    }

    public Integer getMaxSize() {
        return maxSize;
    }

    public void setMaxSize(Integer maxSize) {
        this.maxSize = maxSize;
    }

    public Integer getPoolSize() {
        return poolSize;
    }

    public void setPoolSize(Integer poolSize) {
        this.poolSize = poolSize;
    }

    public Integer getActiveCount() {
        return activeCount;
    }

    public void setActiveCount(Integer activeCount) {
        this.activeCount = activeCount;
    }

    public Integer getQueueSize() {
        return queueSize;
    }

    public void setQueueSize(Integer queueSize) {
        this.queueSize = queueSize;
    }

    public Integer getRemainingCapacity() {
        return remainingCapacity;
    }

    public void setRemainingCapacity(Integer remainingCapacity) {
        this.remainingCapacity = remainingCapacity;
    }

    public Long getCompletedTasks() {
        return completedTasks;
    }

    public String getRejectHandler() {
        return rejectHandler;
    }

    public void setRejectHandler(String rejectHandler) {
        this.rejectHandler = rejectHandler;
    }

    public void setCompletedTasks(Long completedTasks) {
        this.completedTasks = completedTasks;
    }
}

package com.guilu.domain;

/**
 * ExecutorService指标信息
 */
public record ExecutorMetricsInfo(int poolSize, int corePoolSize, int maximumPoolSize,
        int largestPoolSize, int activeCount, int queueSize, int queueRemainingCapacity,
        long taskCount, long completedTaskCount, boolean shutdown, boolean terminated) {}

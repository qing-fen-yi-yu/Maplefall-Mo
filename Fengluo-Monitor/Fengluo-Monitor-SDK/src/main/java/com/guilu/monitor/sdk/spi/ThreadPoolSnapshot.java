package com.guilu.monitor.sdk.spi;

import java.time.Duration;

public record ThreadPoolSnapshot(
        String name,
        int coreSize,
        int maxSize,
        int poolSize,
        int activeCount,
        int largestPoolSize,
        int queueSize,
        int remainingCapacity,
        long taskCount,
        long completedTaskCount,
        long rejectedTaskCount,
        Duration keepAlive,
        String rejectHandler,
        String queueType,
        Long threadId,
        String threadName,
        String timeUnit,
        boolean shutdown,
        boolean terminating,
        boolean terminated) {

    public ThreadPoolSnapshot {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("pool name is required");
        }
        if (keepAlive == null || keepAlive.isNegative()) {
            throw new IllegalArgumentException("keepAlive must not be negative");
        }
    }
    public boolean hasUnboundedQueue() {
        return remainingCapacity == Integer.MAX_VALUE;
    }
}

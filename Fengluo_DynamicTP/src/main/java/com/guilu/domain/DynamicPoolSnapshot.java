package com.guilu.domain;

import com.guilu.DynamicThreadPool.DynamicPool;
import com.guilu.enums.PoolStatus;
import lombok.Getter;
import lombok.Setter;

import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Setter
@Getter
public class DynamicPoolSnapshot extends DefaultPoolSnapshot {

    private Long threadId;
    private String threadName;
    private TimeUnit timeUnit;
    private Map<PoolStatus, Boolean> poolStatus;
    private String queueType;
    private Long taskCount;
    private Long keepAliveTime;
    private Integer largestPoolSize;

    public DynamicPoolSnapshot(String name, Integer coreSize, Integer maxSize, Integer poolSize,
                               Integer activeCount, Integer queueSize, Integer remainingCapacity,
                               Long completedTasks, String rejectHandler) {
        super(name, coreSize, maxSize, poolSize, activeCount, queueSize, remainingCapacity, completedTasks, rejectHandler);
    }

    public DynamicPoolSnapshot(String name, Integer coreSize, Integer maxSize, Integer poolSize,
                               Integer activeCount, Integer queueSize, Integer remainingCapacity,
                               Long completedTasks, String rejectHandler,
                               Long threadId, String threadName, TimeUnit timeUnit,
                               Map<PoolStatus, Boolean> poolStatus, String queueType,
                               Long taskCount, long keepAliveTime, Integer largestPoolSize) {
        super(name, coreSize, maxSize, poolSize, activeCount, queueSize, remainingCapacity, completedTasks, rejectHandler);
        this.threadId = threadId;
        this.threadName = threadName;
        this.timeUnit = timeUnit;
        this.poolStatus = poolStatus;
        this.queueType = queueType;
        this.taskCount = taskCount;
        this.keepAliveTime = keepAliveTime;
        this.largestPoolSize = largestPoolSize;
    }

    public static DynamicPoolSnapshot collectPoolData(DynamicPool dynamicPool) {
        if (dynamicPool == null) {
            return null;
        }

        String name = dynamicPool.getThreadPoolName();
        Integer coreSize = dynamicPool.getCorePoolSize();
        Integer maxSize = dynamicPool.getMaximumPoolSize();
        Integer poolSize = dynamicPool.getPoolSize();
        Integer activeCount = dynamicPool.getActiveCount();
        Integer queueSize = dynamicPool.getQueueSize();
        Integer remainingCapacity = dynamicPool.getQueueRemainingCapacity();
        Long completedTasks = dynamicPool.getCompletedTaskCount();
        String rejectHandler = dynamicPool.getRejectHandler().getClass().getSimpleName();

        Long threadId = dynamicPool.getThreadId();
        String threadName = dynamicPool.getThreadPoolName();
        TimeUnit timeUnit = TimeUnit.valueOf(dynamicPool.getTimeUnit());
        Map<PoolStatus, Boolean> poolStatus = new EnumMap<>(dynamicPool.getPoolStatus());
        String queueType = dynamicPool.getQueueType();
        Long taskCount = dynamicPool.getTaskCount();
        long keepAliveTime = dynamicPool.getKeepAliveTime();
        Integer largestPoolSize = dynamicPool.getLargestPoolSize();

        return new DynamicPoolSnapshot(
                name, coreSize, maxSize, poolSize, activeCount,
                queueSize, remainingCapacity, completedTasks, rejectHandler,
                threadId, threadName, timeUnit, poolStatus,
                queueType, taskCount, keepAliveTime, largestPoolSize
        );
    }

}
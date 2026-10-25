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

    /** Common snapshot for reporter and metrics consumers. Unsupported executor types return null. */
    public static DynamicPoolSnapshot collectPoolData(String registryName, java.util.concurrent.Executor executor) {
        java.util.Objects.requireNonNull(registryName, "registryName");
        java.util.Objects.requireNonNull(executor, "executor");
        if (!(executor instanceof java.util.concurrent.ThreadPoolExecutor pool)) return null;
        DynamicPool dynamic = pool instanceof DynamicPool ? (DynamicPool) pool : null;
        TimeUnit unit = dynamic == null ? TimeUnit.MILLISECONDS : TimeUnit.valueOf(dynamic.getTimeUnit());
        String handler = pool.getRejectedExecutionHandler().getClass().getSimpleName();
        Map<PoolStatus, Boolean> status = new EnumMap<>(PoolStatus.class);
        status.put(PoolStatus.SHUTDOWN, pool.isShutdown());
        status.put(PoolStatus.TERMINATING, pool.isTerminating());
        status.put(PoolStatus.TERMINATED, pool.isTerminated());
        return new DynamicPoolSnapshot(registryName, pool.getCorePoolSize(), pool.getMaximumPoolSize(),
                pool.getPoolSize(), pool.getActiveCount(), pool.getQueue().size(),
                pool.getQueue().remainingCapacity(), pool.getCompletedTaskCount(),
                handler.isEmpty() ? "non" : handler, dynamic == null ? null : dynamic.getThreadId(),
                registryName, unit, Map.copyOf(status), pool.getQueue().getClass().getSimpleName(),
                pool.getTaskCount(), pool.getKeepAliveTime(unit), pool.getLargestPoolSize());
    }

    public static DynamicPoolSnapshot collectPoolData(DynamicPool dynamicPool) {
        return dynamicPool == null ? null : collectPoolData(dynamicPool.getThreadPoolName(), dynamicPool);
    }
}

package com.guilu.domain;

import com.guilu.DynamicThreadPool.DynamicPool;
import com.guilu.DynamicThreadPool.FLDynamicReject.DynamicRejectHandler;
import com.guilu.DynamicThreadPool.FLDynamicReject.RejectionStatsSource;
import com.guilu.enums.PoolStatus;
import lombok.Getter;
import lombok.Setter;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
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
    /**
     * 累计拒绝次数；具体策略未暴露统计时为 -1。
     */
    private Long rejectedTasks = -1L;

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
        this(name, coreSize, maxSize, poolSize, activeCount, queueSize, remainingCapacity, completedTasks,
                rejectHandler, threadId, threadName, timeUnit, poolStatus, queueType, taskCount,
                keepAliveTime, largestPoolSize, -1L);
    }

    public DynamicPoolSnapshot(String name, Integer coreSize, Integer maxSize, Integer poolSize,
                               Integer activeCount, Integer queueSize, Integer remainingCapacity,
                               Long completedTasks, String rejectHandler,
                               Long threadId, String threadName, TimeUnit timeUnit,
                               Map<PoolStatus, Boolean> poolStatus, String queueType,
                               Long taskCount, long keepAliveTime, Integer largestPoolSize,
                               Long rejectedTasks) {
        super(name, coreSize, maxSize, poolSize, activeCount, queueSize, remainingCapacity, completedTasks, rejectHandler);
        this.threadId = threadId;
        this.threadName = threadName;
        this.timeUnit = timeUnit;
        this.poolStatus = poolStatus;
        this.queueType = queueType;
        this.taskCount = taskCount;
        this.keepAliveTime = keepAliveTime;
        this.largestPoolSize = largestPoolSize;
        this.rejectedTasks = rejectedTasks == null ? -1L : rejectedTasks;
    }

    /** Common snapshot for reporter and metrics consumers. Unsupported executor types return null. */
    public static DynamicPoolSnapshot collectPoolData(String registryName, Executor executor) {
        Objects.requireNonNull(registryName, "registryName");
        Objects.requireNonNull(executor, "executor");
        if (!(executor instanceof ThreadPoolExecutor pool)) {
            return null;
        }
        DynamicPool dynamic = pool instanceof DynamicPool ? (DynamicPool) pool : null;
        TimeUnit unit = resolveTimeUnit(dynamic);
        RejectedExecutionHandler handler = pool.getRejectedExecutionHandler();
        Map<PoolStatus, Boolean> status = new EnumMap<>(PoolStatus.class);
        status.put(PoolStatus.SHUTDOWN, pool.isShutdown());
        status.put(PoolStatus.TERMINATING, pool.isTerminating());
        status.put(PoolStatus.TERMINATED, pool.isTerminated());
        String threadName = dynamic == null || dynamic.getThreadPoolName() == null
                ? registryName : dynamic.getThreadPoolName();
        long rejected = handler instanceof RejectionStatsSource stats ? stats.rejectedCount() : -1L;
        return new DynamicPoolSnapshot(registryName, pool.getCorePoolSize(), pool.getMaximumPoolSize(),
                pool.getPoolSize(), pool.getActiveCount(), pool.getQueue().size(),
                pool.getQueue().remainingCapacity(), pool.getCompletedTaskCount(),
                handlerName(handler), dynamic == null ? null : dynamic.getThreadId(),
                threadName, unit, Map.copyOf(status), pool.getQueue().getClass().getSimpleName(),
                pool.getTaskCount(), pool.getKeepAliveTime(unit), pool.getLargestPoolSize(), rejected);
    }

    public static DynamicPoolSnapshot collectPoolData(DynamicPool dynamicPool) {
        return dynamicPool == null ? null : collectPoolData(dynamicPool.getThreadPoolName(), dynamicPool);
    }

    private static TimeUnit resolveTimeUnit(DynamicPool dynamic) {
        if (dynamic == null) {
            return TimeUnit.MILLISECONDS;
        }
        String configured = dynamic.getTimeUnit();
        if (configured == null) {
            return TimeUnit.MILLISECONDS;
        }
        try {
            return TimeUnit.valueOf(configured);
        } catch (IllegalArgumentException e) {
            return TimeUnit.MILLISECONDS;
        }
    }

    private static String handlerName(RejectedExecutionHandler handler) {
        if (handler instanceof DynamicRejectHandler dynamic) {
            return dynamic.getType().name();
        }
        String name = handler.getClass().getSimpleName();
        return name.isEmpty() ? "none" : name;
    }
}

package com.guilu.executor;

import com.guilu.domain.ExecutorMetricsInfo;
import com.guilu.domain.DynamicPoolSnapshot;
import com.guilu.Manager.reporter.PoolReporter;
import com.guilu.enums.PoolStatus;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

public final class ExecutorMetrics {
    private ExecutorMetrics() {}

    public static DynamicPoolSnapshot snapshot(String registryName, Executor executor) {
        return DynamicPoolSnapshot.collectPoolData(registryName, executor);
    }

    public static Map<String, DynamicPoolSnapshot> snapshot(PoolReporter reporter) {
        return Objects.requireNonNull(reporter, "reporter").snapshot();
    }

    public static ExecutorMetricsInfo snapshot(ThreadPoolExecutor executor) {
        Objects.requireNonNull(executor, "executor");
        DynamicPoolSnapshot info = snapshot("unknown", executor);
        return new ExecutorMetricsInfo(info.getPoolSize(), info.getCoreSize(), info.getMaxSize(),
                info.getLargestPoolSize(), info.getActiveCount(), info.getQueueSize(), info.getRemainingCapacity(),
                info.getTaskCount(), info.getCompletedTasks(), info.getPoolStatus().get(PoolStatus.SHUTDOWN),
                info.getPoolStatus().get(PoolStatus.TERMINATED));
    }
}

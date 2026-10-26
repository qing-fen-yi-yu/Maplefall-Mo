package com.guilu.metrics;

import com.guilu.Manager.reporter.PoolReporter;
import com.guilu.domain.DynamicPoolSnapshot;
import com.guilu.enums.PoolStatus;
import com.guilu.monitor.sdk.spi.ThreadPoolSnapshot;
import com.guilu.monitor.sdk.spi.ThreadPoolSnapshotSource;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public final class PoolReporterSnapshotSource implements ThreadPoolSnapshotSource {

    private final PoolReporter reporter;

    public PoolReporterSnapshotSource(PoolReporter reporter) {
        this.reporter = Objects.requireNonNull(reporter, "reporter");
    }

    @Override
    public Map<String, ThreadPoolSnapshot> snapshot() {
        Map<String, DynamicPoolSnapshot> source = reporter.snapshot();
        if (source == null || source.isEmpty()) {
            return Map.of();
        }
        Map<String, ThreadPoolSnapshot> result = new HashMap<>(source.size());
        source.forEach((name, snapshot) -> {
            if (name != null && snapshot != null) {
                result.put(name, map(name, snapshot));
            }
        });
        return Map.copyOf(result);
    }

    static ThreadPoolSnapshot map(String registryName, DynamicPoolSnapshot snapshot) {
        TimeUnit unit = snapshot.getTimeUnit() == null ? TimeUnit.MILLISECONDS : snapshot.getTimeUnit();
        long keepAlive = snapshot.getKeepAliveTime() == null ? 0L : snapshot.getKeepAliveTime();
        Map<PoolStatus, Boolean> status = snapshot.getPoolStatus();
        String name = snapshot.getName() == null || snapshot.getName().isBlank()
                ? registryName : snapshot.getName();
        return new ThreadPoolSnapshot(
                name,
                intValue(snapshot.getCoreSize()),
                intValue(snapshot.getMaxSize()),
                intValue(snapshot.getPoolSize()),
                intValue(snapshot.getActiveCount()),
                intValue(snapshot.getLargestPoolSize()),
                intValue(snapshot.getQueueSize()),
                snapshot.getRemainingCapacity() == null
                        ? Integer.MAX_VALUE : snapshot.getRemainingCapacity(),
                longValue(snapshot.getTaskCount()),
                longValue(snapshot.getCompletedTasks()),
                snapshot.getRejectedTasks() == null ? -1L : snapshot.getRejectedTasks(),
                Duration.ofNanos(unit.toNanos(keepAlive)),
                snapshot.getRejectHandler(),
                snapshot.getQueueType(),
                snapshot.getThreadId(),
                snapshot.getThreadName(),
                unit.name(),
                flag(status, PoolStatus.SHUTDOWN),
                flag(status, PoolStatus.TERMINATING),
                flag(status, PoolStatus.TERMINATED));
    }

    private static boolean flag(Map<PoolStatus, Boolean> status, PoolStatus key) {
        return status != null && Boolean.TRUE.equals(status.get(key));
    }

    private static int intValue(Integer value) {
        return value == null ? 0 : value;
    }

    private static long longValue(Long value) {
        return value == null ? 0L : value;
    }
}

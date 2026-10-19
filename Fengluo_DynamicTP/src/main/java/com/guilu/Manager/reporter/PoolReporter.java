package com.guilu.Manager.reporter;

import com.guilu.DynamicThreadPool.DynamicPool;
import com.guilu.Manager.PoolManager;
import com.guilu.domain.DynamicPoolSnapshot;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

@Slf4j
public class PoolReporter extends PoolManager{

    private final String applicationName;

    public PoolReporter(Environment env) {
        this.applicationName = env.getProperty("spring.application.name", "unknown");
    }

    public Map<String, DynamicPoolSnapshot> snapshot() {
        Map<String, DynamicPoolSnapshot> result = new HashMap<>();
        getAlivePool().forEach((name, executor) -> {
            try {
                DynamicPoolSnapshot snapshot = buildSnapshot(name, executor);
                if (snapshot != null) {
                    result.put(name, snapshot);
                }
            } catch (Exception e) {
                log.warn("采集线程池快照失败: name={}, type={}", name,
                        executor == null ? "null" : executor.getClass().getName(), e);
            }
        });
        return Map.copyOf(result);
    }

    private DynamicPoolSnapshot buildSnapshot(String registryName, Executor executor) {
        if (executor instanceof DynamicPool dynamicPool) {
            DynamicPoolSnapshot snapshot = DynamicPoolSnapshot.collectPoolData(dynamicPool);
            if (snapshot != null && !registryName.equals(snapshot.getThreadName())) {
                snapshot.setThreadName(registryName);
            }
            return snapshot;
        } else if (executor instanceof ThreadPoolExecutor pool) {
            return new DynamicPoolSnapshot(
                    registryName,
                    pool.getCorePoolSize(),
                    pool.getMaximumPoolSize(),
                    pool.getPoolSize(),
                    pool.getActiveCount(),
                    pool.getQueue().size(),
                    pool.getQueue().remainingCapacity(),
                    pool.getCompletedTaskCount(),
                    safeRejectHandler(pool)
            );
        }
        return null;
    }

    private static String safeRejectHandler(ThreadPoolExecutor pool) {
        String simpleName = pool.getRejectedExecutionHandler().getClass().getSimpleName();
        return simpleName.isEmpty() ? "non" : simpleName;
    }
}
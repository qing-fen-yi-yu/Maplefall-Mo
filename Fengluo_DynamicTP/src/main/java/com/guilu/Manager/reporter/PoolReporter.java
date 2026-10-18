package com.guilu.Manager.reporter;

import com.guilu.Manager.PoolManager;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;

//线程池信息上报器
public class PoolReporter extends PoolManager {
    public Map<String, PoolSnapshot> snapshot() {
        Map<String, PoolSnapshot> result = new HashMap<>();
        getAlivePool().forEach((name, executor) -> {
            if (executor instanceof ThreadPoolExecutor pool) {
                result.put(name, new PoolSnapshot(name, pool.getCorePoolSize(), pool.getMaximumPoolSize(),
                        pool.getPoolSize(), pool.getActiveCount(), pool.getQueue().size(),
                        pool.getQueue().remainingCapacity(), pool.getCompletedTaskCount(), pool.isShutdown()));
            }
        });
        return Map.copyOf(result);
    }
    public record PoolSnapshot(String name, int coreSize, int maxSize, int poolSize, int activeCount,
                               int queueSize, int remainingCapacity, long completedTasks, boolean shutdown) {
    }
}

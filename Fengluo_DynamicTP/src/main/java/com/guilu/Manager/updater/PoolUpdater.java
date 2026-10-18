package com.guilu.Manager.updater;

import com.guilu.Manager.PoolManager;
import com.guilu.DynamicThreadPool.FLBlockingQueue.CapacityBlockingQueue;
import com.guilu.exception.BusinessException.ThreadPoolException;

import java.util.concurrent.*;

//线程池动态更新接收器
public class PoolUpdater extends PoolManager {
    private static final PoolUpdater INSTANCE = new PoolUpdater();
    public static PoolUpdater of() { return INSTANCE; }
    private PoolUpdater() {}
    public void removePoolByBean(String name, Executor executor) {
        removePoolByRegis(name, executor);
    }

    public void update(String name, int coreSize, int maxSize, long keepAlive, TimeUnit unit, Integer queueCapacity) {
        if (name == null || name.isBlank() || unit == null || coreSize < 0 || maxSize < 1 || coreSize > maxSize || keepAlive < 0)
            throw new ThreadPoolException("Invalid pool settings");
        Executor registered = getPoolByAlive(name);
        if (!(registered instanceof ThreadPoolExecutor pool))
            throw new ThreadPoolException("No adjustable pool: " + name);
        synchronized (pool) {
            if (pool.isShutdown())
                throw new ThreadPoolException("Pool has shut down: " + name);
            if (pool.allowsCoreThreadTimeOut() && keepAlive == 0)
                throw new ThreadPoolException("Core timeout requires positive keepAlive");
            if (queueCapacity != null && (queueCapacity <= 0 ||
                    !(pool.getQueue() instanceof CapacityBlockingQueue<?>)))
                throw new ThreadPoolException("Queue does not support the requested capacity");
            if (maxSize < pool.getCorePoolSize()) {
                pool.setCorePoolSize(coreSize); pool.setMaximumPoolSize(maxSize);
            } else {
                pool.setMaximumPoolSize(maxSize); pool.setCorePoolSize(coreSize);
            }
            pool.setKeepAliveTime(keepAlive, unit);
            if (queueCapacity != null) {
                ((CapacityBlockingQueue<?>) pool.getQueue()).setCapacity(queueCapacity);
            }
        }
    }
}

package com.guilu.Manager.updater;

import com.guilu.Manager.PoolManager;
import com.guilu.DynamicThreadPool.FLBlockingQueue.CapacityBlockingQueue;
import com.guilu.domain.po.ThreadPool.PoolUpdateConfig;
import com.guilu.domain.po.ThreadPool.ThreadPoolConfig;
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
        if (name == null || name.isBlank()) {
            throw new ThreadPoolException("线程池名称不能为空");
        }
        if (unit == null) {
            throw new ThreadPoolException("时间单位不能为空: " + name);
        }
        if (coreSize < 0) {
            throw new ThreadPoolException("核心线程数非法: " + coreSize);
        }
        if (maxSize < 1 || coreSize > maxSize) {
            throw new ThreadPoolException("最大线程数非法或小于核心线程数: " + maxSize);
        }
        if (keepAlive < 0) {
            throw new ThreadPoolException("keepAlive 非法: " + keepAlive);
        }

        Executor registered = PoolManager.getPoolByAlive(name);
        if (!(registered instanceof ThreadPoolExecutor pool)) {
            throw new ThreadPoolException("未找到可调整的线程池: " + name);
        }

        // 队列类型校验可放在锁外，队列类型不会变
        if (queueCapacity != null) {
            if (queueCapacity <= 0) {
                throw new ThreadPoolException("队列容量非法: " + queueCapacity);
            }
            if (!(pool.getQueue() instanceof CapacityBlockingQueue)) {
                throw new ThreadPoolException("队列不支持调整容量: " + name);
            }
        }
        synchronized (pool) {
            if (pool.isShutdown()) {
                throw new ThreadPoolException("线程池已关闭: " + name);
            }
            if (pool.allowsCoreThreadTimeOut() && keepAlive == 0) {
                throw new ThreadPoolException("core 允许超时时 keepAlive 必须为正: " + name);
            }

            int currentCore = pool.getCorePoolSize();
            int currentMax = pool.getMaximumPoolSize();

            if (coreSize > currentMax) {
                pool.setMaximumPoolSize(maxSize);
                pool.setCorePoolSize(coreSize);
            } else if (maxSize < currentCore) {
                pool.setCorePoolSize(coreSize);
                pool.setMaximumPoolSize(maxSize);
            } else {
                pool.setCorePoolSize(coreSize);
                pool.setMaximumPoolSize(maxSize);
            }

            pool.setKeepAliveTime(keepAlive, unit);

            if (queueCapacity != null) {
                CapacityBlockingQueue<Runnable> queue =
                        (CapacityBlockingQueue<Runnable>) pool.getQueue();
                queue.setCapacity(queueCapacity);
            }
        }
    }
}

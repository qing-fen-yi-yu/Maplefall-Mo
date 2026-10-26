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

    /**
     * 动态更新线程池参数。null 字段表示保持不变，只有显式传入的值才会生效。
     */
    public void update(String name, Integer coreSize, Integer maxSize, Long keepAlive,
                       TimeUnit unit, Integer queueCapacity) {
        if (name == null || name.isBlank()) {
            throw new ThreadPoolException("线程池名称不能为空");
        }
        if (keepAlive != null && unit == null) {
            throw new ThreadPoolException("时间单位不能为空: " + name);
        }
        if (coreSize != null && coreSize < 0) {
            throw new ThreadPoolException("核心线程数非法: " + coreSize);
        }
        if (maxSize != null && maxSize < 1) {
            throw new ThreadPoolException("最大线程数非法: " + maxSize);
        }
        if (keepAlive != null && keepAlive < 0) {
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

            int currentCore = pool.getCorePoolSize();
            int currentMax = pool.getMaximumPoolSize();
            int targetCore = coreSize == null ? currentCore : coreSize;
            int targetMax = maxSize == null ? currentMax : maxSize;
            if (targetCore > targetMax) {
                throw new ThreadPoolException(
                        "最大线程数非法或小于核心线程数: " + targetMax + " < " + targetCore);
            }
            if (pool.allowsCoreThreadTimeOut() && keepAlive != null && keepAlive == 0) {
                throw new ThreadPoolException("core 允许超时时 keepAlive 必须为正: " + name);
            }

            if (targetMax < currentCore) {
                pool.setCorePoolSize(targetCore);
                pool.setMaximumPoolSize(targetMax);
            } else if (targetCore > currentMax) {
                pool.setMaximumPoolSize(targetMax);
                pool.setCorePoolSize(targetCore);
            } else {
                pool.setCorePoolSize(targetCore);
                pool.setMaximumPoolSize(targetMax);
            }

            if (keepAlive != null) {
                pool.setKeepAliveTime(keepAlive, unit);
            }

            if (queueCapacity != null) {
                CapacityBlockingQueue<Runnable> queue =
                        (CapacityBlockingQueue<Runnable>) pool.getQueue();
                queue.setCapacity(queueCapacity);
            }
        }
    }
}

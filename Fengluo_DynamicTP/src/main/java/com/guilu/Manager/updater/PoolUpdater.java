package com.guilu.Manager.updater;

import com.guilu.Manager.PoolManager;
import com.guilu.DynamicThreadPool.FLBlockingQueue.CapacityBlockingQueue;
import com.guilu.Manager.register.PoolRegister;
import com.guilu.exception.BusinessException.ThreadPoolException;
import com.guilu.domain.po.ThreadPool.BaseThreadPoolConfig;
import com.guilu.enums.RejectHandlerType;
import com.guilu.DynamicThreadPool.FLDynamicReject.DynamicRejectHandler;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;

import java.util.Collection;
import java.util.Objects;

import java.util.concurrent.*;

//线程池动态更新接收器
public class PoolUpdater extends PoolManager {
    private final DefaultListableBeanFactory beanFactory;
    public PoolUpdater(DefaultListableBeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }
    public void removePoolByBean(String name, Executor executor) {
        removePoolByRegis(name, executor);
        removeBean(name);
    }

    public void removeByName(String name) {
        removePoolByBean(name, null);
    }
    //卸载线程池bean
    private void removeBean(String name) {
        if(beanFactory.containsBean(name)){
            beanFactory.removeBeanDefinition(name);
        }
    }
    /**
     * 动态更新线程池参数。null 字段表示保持不变，只有显式传入的值才会生效。
     */
    public void update(String name, Integer coreSize, Integer maxSize, Long keepAlive,
                       TimeUnit unit, Integer queueCapacity) {
        update(name, coreSize, maxSize, keepAlive, unit, queueCapacity, null, null, true);
    }

    public void update(BaseThreadPoolConfig config) {
        applyConfig(config, true);
    }
    public void update(Collection<BaseThreadPoolConfig> configs) {
        configs.forEach(this::update);
    }
    public void validate(BaseThreadPoolConfig config) {
        applyConfig(config, false);
    }

    private void applyConfig(BaseThreadPoolConfig config, boolean apply) {
        Objects.requireNonNull(config, "线程池配置不能为空");
        Long keepAlive = config.getKeepAliveSeconds() == null ? null : config.getKeepAliveSeconds().longValue();
        update(config.getPoolName(), config.getCorePoolSize(), config.getMaxPoolSize(), keepAlive,
                TimeUnit.SECONDS, config.getQueueCapacity(), config.getAllowCoreThreadTimeout(),
                config.getRejectedPolicy(), apply);
    }

    private void update(String name, Integer coreSize, Integer maxSize, Long keepAlive,
                        TimeUnit unit, Integer queueCapacity, Integer coreTimeout,
                        String rejectedPolicy, boolean apply) {
        if (coreTimeout != null && coreTimeout != 0 && coreTimeout != 1) {
            throw new ThreadPoolException("核心线程超时标志必须为 0 或 1");
        }
        RejectedExecutionHandler handler = rejectedPolicy == null ? null : RejectHandlerType.of(rejectedPolicy);
        if (rejectedPolicy != null && handler == null) {
            throw new ThreadPoolException("拒绝策略非法: " + rejectedPolicy);
        }
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
            boolean targetTimeout = coreTimeout == null ? pool.allowsCoreThreadTimeOut() : coreTimeout == 1;
            long targetKeepAlive = keepAlive == null ? pool.getKeepAliveTime(TimeUnit.NANOSECONDS) : keepAlive;
            if (targetTimeout && targetKeepAlive == 0) {
                throw new ThreadPoolException("core 允许超时时 keepAlive 必须为正: " + name);
            }
            if (!apply) {
                return;
            }

            // 先关闭超时，才能将存活时间降为零；开启超时则在存活时间更新后执行。
            if (!targetTimeout && pool.allowsCoreThreadTimeOut()) {
                pool.allowCoreThreadTimeOut(false);
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
            if (coreTimeout != null) {
                pool.allowCoreThreadTimeOut(targetTimeout);
            }
            // 策略未变化时保留原处理器及其累计拒绝次数。
            if (handler != null && !(pool.getRejectedExecutionHandler() instanceof DynamicRejectHandler current
                    && current.getType() == RejectHandlerType.from(rejectedPolicy))) {
                pool.setRejectedExecutionHandler(handler);
            }
        }
    }

    public boolean add(ExecutorService executorService,String poolName) {
        PoolRegister.registerThreadPool(executorService, poolName);
        return true;
    }
}

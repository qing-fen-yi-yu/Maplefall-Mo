package com.guilu.Manager.updater;

import com.guilu.DynamicThreadPool.DynamicPool;
import com.guilu.DynamicThreadPool.FLBlockingQueue.CapacityBlockingQueue;
import com.guilu.DynamicThreadPool.FLDynamicReject.DynamicRejectHandler;
import com.guilu.DynamicThreadPool.FLThreadFactory.DynamicThreadFactory;
import com.guilu.Event.TPConfigUpdaterEvent;
import com.guilu.Event.TPCreateEvent;
import com.guilu.domain.po.ThreadPool.BaseThreadPoolConfig;
import com.guilu.domain.po.ThreadPool.CommonThreadPoolConfig;
import com.guilu.domain.po.ThreadPool.PoolCreateConfig;
import com.guilu.enums.TPType;
import com.guilu.enums.ThreadFactoryType;
import com.guilu.utils.BeanUtils;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public interface TPUpdater {
    //发布线程池更新配置
    boolean publishTPConfigEvent(TPConfigUpdaterEvent event);
    //执行线程池的更新操作
    boolean updateTP(BaseThreadPoolConfig threadPoolConfig);

    boolean addTPConfig(TPCreateEvent event) throws ExecutionException, InterruptedException;

    default ExecutorService createExecutorService(PoolCreateConfig createConfig){
        TPType executorType = createConfig.getExecutorType();
        Integer corePoolSize = createConfig.getCorePoolSize();
        Integer maxPoolSize = createConfig.getMaxPoolSize();
        Integer queueCapacity = createConfig.getQueueCapacity();
        Integer keepAliveSeconds = createConfig.getKeepAliveSeconds();
        Integer allowCoreThreadTimeout = createConfig.getAllowCoreThreadTimeout();
        String rejectedPolicy = createConfig.getRejectedPolicy();
        if(executorType.equals(TPType.DYNAMIC)){
            CommonThreadPoolConfig commonThreadPoolConfig =
                    BeanUtils.copyBean(createConfig, CommonThreadPoolConfig.class);
            commonThreadPoolConfig.setThreadFactory(ThreadFactoryType.DYNAMIC);
            commonThreadPoolConfig.setRejectedPolicy(rejectedPolicy);
            return new DynamicPool(commonThreadPoolConfig,null);
        } else if(executorType.equals(TPType.DEFAULT)){
            ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(
                    corePoolSize, maxPoolSize,
                    keepAliveSeconds, TimeUnit.SECONDS,
                    new CapacityBlockingQueue<>(queueCapacity),
                    DynamicThreadFactory.getInstance(),
                    new DynamicRejectHandler());
            threadPoolExecutor.allowCoreThreadTimeOut(allowCoreThreadTimeout == 1);
            return threadPoolExecutor;
        } else {
            throw new IllegalArgumentException("当前不支持的线程池类型");
        }
    }

    void removeDynamicTP(String poolName, String serviceName, String instanceConfig) throws ExecutionException, InterruptedException;
}

package com.guilu.config;

import com.guilu.DynamicThreadPool.DynamicPool;
import com.guilu.annotation.DynamicThreadPool;
import com.guilu.domain.po.ThreadPool.ThreadPoolConfig;
import com.guilu.enums.ThreadFactoryType;
import com.guilu.utils.RandomUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
public class DynamicPoolTestConfig {
    @DynamicThreadPool(name = "test3")
    private final ExecutorService dynamicThreadPool3 = Executors.newCachedThreadPool();
    @Bean
    @DynamicThreadPool(name = "test1")
    public ExecutorService threadPoolExecutor() {
        return Executors.newFixedThreadPool(10);
    }
    @DynamicThreadPool(name = "test2-1")
    public DynamicPool threadPoolExecutor2() {
        return getDynamicPool();
    }
    @DynamicThreadPool
    @Bean("test2-2")
    public  DynamicPool dynamicThreadPool() {
        return getDynamicPool();
    }
    private DynamicPool getDynamicPool() {
        ThreadPoolConfig threadPoolConfig = new ThreadPoolConfig();
        threadPoolConfig.setThreadPoolName("test2");
        threadPoolConfig.setThreadFactory(ThreadFactoryType.DYNAMIC);
        threadPoolConfig.setCoreSize(10);
        threadPoolConfig.setMaxPoolSize(10);
        threadPoolConfig.setKeepAliveTime(60);
        threadPoolConfig.setTimeUnit("SECONDS");
        threadPoolConfig.setRejectHandler("ABORT");
        threadPoolConfig.setBlockingQueueType("LinkedBlockingQueue");
        threadPoolConfig.setBlockingQueueSize(100);
        threadPoolConfig.setCreateUser(1L);
        return new DynamicPool(threadPoolConfig, RandomUtils.randomLong());
    }
}

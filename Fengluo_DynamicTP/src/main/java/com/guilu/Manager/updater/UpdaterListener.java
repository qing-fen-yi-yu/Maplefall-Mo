package com.guilu.Manager.updater;

import com.guilu.Event.TPConfigUpdaterEvent;
import com.guilu.domain.po.ThreadPool.PoolUpdateConfig;
import org.springframework.context.event.EventListener;

import java.util.concurrent.TimeUnit;

//线程池配置更新器事件监听器
public class UpdaterListener {
    @EventListener(TPConfigUpdaterEvent.class)
    public void updateThreadPool(TPConfigUpdaterEvent event) {
        PoolUpdateConfig poolConfig = event.getPoolConfig();
        String name = poolConfig.getThreadPoolName();
        Integer maxPoolSize = poolConfig.getMaxPoolSize();
        TimeUnit unit = poolConfig.getUnit();
        Integer coreSize = poolConfig.getCoreSize();
        Integer queueCapacity = poolConfig.getQueueCapacity();
        Long keepAliveTime = poolConfig.getKeepAlive();
        PoolUpdater.of().update(name,coreSize,maxPoolSize,keepAliveTime,unit,queueCapacity);
    }
}

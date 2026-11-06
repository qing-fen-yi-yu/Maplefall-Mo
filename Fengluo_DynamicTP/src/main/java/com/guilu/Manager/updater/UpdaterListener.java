package com.guilu.Manager.updater;

import com.guilu.Event.TPConfigUpdaterEvent;
import com.guilu.domain.po.ThreadPool.PoolUpdateConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;

//线程池配置更新器事件监听器
@Slf4j
public class UpdaterListener {
    @EventListener(TPConfigUpdaterEvent.class)
    public void updateThreadPool(TPConfigUpdaterEvent event) {
        if (event == null) {
            return;
        }
        PoolUpdateConfig poolConfig = event.getPoolConfig();
        if (poolConfig == null) {
            log.warn("线程池更新事件缺少配置，忽略: {}", event);
            return;
        }
        PoolUpdater.of().update(poolConfig);
    }
}

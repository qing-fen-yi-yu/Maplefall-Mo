package com.guilu.Manager.updater;

import com.guilu.Event.TPConfigUpdaterEvent;
import com.guilu.domain.po.ThreadPool.BaseThreadPoolConfig;

public interface TPUpdater {
    //发布线程池更新配置
    boolean publishTPConfigEvent(TPConfigUpdaterEvent event);
    //执行线程池的更新操作
    boolean updateTP(BaseThreadPoolConfig threadPoolConfig);
}

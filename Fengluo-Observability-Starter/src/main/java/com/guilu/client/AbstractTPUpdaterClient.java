package com.guilu.client;

import com.guilu.Event.TPConfigUpdaterEvent;
import com.guilu.Manager.updater.TPUpdater;
import com.guilu.domain.po.ThreadPool.BaseThreadPoolConfig;

public abstract class AbstractTPUpdaterClient implements TPUpdater {
    @Override
    public abstract boolean publishTPConfigEvent(TPConfigUpdaterEvent event);

    @Override
    public abstract boolean updateTP(BaseThreadPoolConfig threadPoolConfig);
}

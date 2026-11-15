package com.guilu.client;

import com.guilu.Event.TPConfigUpdaterEvent;
import com.guilu.Manager.updater.TPUpdater;
import com.guilu.domain.po.ThreadPool.BaseThreadPoolConfig;

public abstract class AbstractTPUpdaterClient implements TPUpdater {
    private String prefix;
    public String getPrefix() {
        if(!prefix.isBlank()){
            return prefix;
        }
        return "";
    }
    public  AbstractTPUpdaterClient(String prefix) {
        this.prefix = prefix;
    }

    @Override
    public abstract boolean publishTPConfigEvent(TPConfigUpdaterEvent event);

    @Override
    public abstract boolean updateTP(BaseThreadPoolConfig threadPoolConfig);
}

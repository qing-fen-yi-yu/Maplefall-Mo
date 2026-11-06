package com.guilu.Event;

import com.guilu.domain.po.ThreadPool.PoolUpdateConfig;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.util.concurrent.TimeUnit;

/**
 * 关联至PoolUpdater
 * @author 归鹭
 */
@Getter
public class TPConfigUpdaterEvent extends ApplicationEvent {
    private final PoolUpdateConfig poolConfig;
    public TPConfigUpdaterEvent(Object source,PoolUpdateConfig poolConfig) {
        super(source);
        this.poolConfig = poolConfig;
    }
}

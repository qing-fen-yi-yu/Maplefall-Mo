package com.guilu.Event;

import com.guilu.domain.po.ThreadPool.PoolCreateConfig;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class TPCreateEvent extends ApplicationEvent {
    private final PoolCreateConfig createConfig;
    public TPCreateEvent(Object source, PoolCreateConfig createConfig) {
        super(source);
        this.createConfig = createConfig;
    }

}

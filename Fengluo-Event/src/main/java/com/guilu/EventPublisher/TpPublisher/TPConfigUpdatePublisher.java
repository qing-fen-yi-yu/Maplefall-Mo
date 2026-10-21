package com.guilu.EventPublisher.TpPublisher;

import com.guilu.Event.TPConfigUpdaterEvent;
import com.guilu.EventPublisher.FLSpringEventPublisher;
import com.guilu.domain.po.ThreadPool.ThreadPoolConfig;
import com.guilu.exception.BusinessException.BadDataException;

public final class TPConfigUpdatePublisher {
    public TPConfigUpdatePublisher(){}
    public static void publish(TPConfigUpdaterEvent event){
        if(event.getPoolConfig() == null){
            throw new BadDataException("PoolConfig data is null");
        }
        FLSpringEventPublisher.publish(event);
    }
}

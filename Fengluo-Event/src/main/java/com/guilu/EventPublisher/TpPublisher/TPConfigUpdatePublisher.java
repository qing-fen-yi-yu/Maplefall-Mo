package com.guilu.EventPublisher.TpPublisher;

import com.guilu.Event.TPConfigUpdaterEvent;
import com.guilu.Event.TPRejectEvent;
import com.guilu.EventPublisher.AbstractEventPublisher;
import com.guilu.EventPublisher.FLSpringEventPublisher;
import com.guilu.exception.BusinessException.BadDataException;

public final class TPConfigUpdatePublisher extends AbstractEventPublisher {
    public TPConfigUpdatePublisher(){}
    public static void publish(TPConfigUpdaterEvent event){
        if(event.getPoolConfig() == null){
            throw new BadDataException("PoolConfig data is null");
        }
    }
}

package com.guilu.EventPublisher;

import com.guilu.Event.TPRejectEvent;
import org.springframework.lang.NonNull;

public abstract class AbstractEventPublisher {
    public static void publishEvent(@NonNull Object event){
        FLSpringEventPublisher.publish(event);
    }
}

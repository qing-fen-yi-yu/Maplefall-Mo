package com.guilu.EventPublisher.TpPublisher;

import com.guilu.Event.TPRejectEvent;
import com.guilu.EventPublisher.FLSpringEventPublisher;
import org.springframework.lang.NonNull;

public final class TPRejectPublisher {

    private TPRejectPublisher() {}

    public static void publishRejectEvent(@NonNull TPRejectEvent event) {
        FLSpringEventPublisher.publish(event);
    }
}

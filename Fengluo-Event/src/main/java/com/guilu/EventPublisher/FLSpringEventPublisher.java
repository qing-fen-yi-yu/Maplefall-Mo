package com.guilu.EventPublisher;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

/**
 * 事件发布器核心
 */
@Slf4j
@Component
public class FLSpringEventPublisher  implements ApplicationEventPublisherAware {
    private static volatile ApplicationEventPublisher delegate;

    @Override
    public void setApplicationEventPublisher(@NonNull ApplicationEventPublisher publisher) {
        delegate = publisher;
    }

    public static void publish(Object event) {
        ApplicationEventPublisher p = delegate;
        if (p == null || event == null) {
            return;
        }
        try {
            p.publishEvent(event);
        } catch (Exception e) {
            log.error("publish event error",e);
        }
    }

    public static boolean isReady() {
        return delegate != null;
    }
}

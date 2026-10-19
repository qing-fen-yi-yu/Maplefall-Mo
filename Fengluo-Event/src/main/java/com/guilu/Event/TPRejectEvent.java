package com.guilu.Event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.time.Clock;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 线程池拒绝事件。
 */
@Getter
public class TPRejectEvent extends ApplicationEvent {

    /** 累计拒绝次数，由线程池拒绝策略维护 */
    private final Long rejectCount;
    /** 拒绝策略类型 */
    private final String rejectHandlerType;
    /** 触发事件的线程池；若担心生命周期，可改为仅存 poolName */
    private final ThreadPoolExecutor poolExecutor;

    public TPRejectEvent(Object source,
                         Long rejectCount,
                         String rejectHandlerType,
                         ThreadPoolExecutor poolExecutor) {
        super(source);
        this.rejectCount = rejectCount;
        this.rejectHandlerType = rejectHandlerType;
        this.poolExecutor = poolExecutor;
    }

    public TPRejectEvent(Object source,
                         Clock clock,
                         Long rejectCount,
                         String rejectHandlerType,
                         ThreadPoolExecutor poolExecutor) {
        super(source, clock);
        this.rejectCount = rejectCount;
        this.rejectHandlerType = rejectHandlerType;
        this.poolExecutor = poolExecutor;
    }


    public static TPRejectEvent createEvent(Object source,
                                            Long rejectCount,
                                            String rejectHandlerType,
                                            ThreadPoolExecutor poolExecutor) {
        return new TPRejectEvent(source, rejectCount, rejectHandlerType, poolExecutor);
    }
}
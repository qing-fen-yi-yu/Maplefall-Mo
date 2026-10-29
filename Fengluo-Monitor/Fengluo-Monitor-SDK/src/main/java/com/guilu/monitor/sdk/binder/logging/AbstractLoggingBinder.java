package com.guilu.monitor.sdk.binder.logging;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;

import java.util.List;

public abstract class AbstractLoggingBinder implements MeterBinder {
    static final String EVENTS = "log4j2.events";
    static final List<String> LEVELS =
            List.of("trace", "debug", "info", "warn", "error");
    public abstract void close();
    public void binderAllLevelCounter(MeterRegistry meterRegistry){
        for (String level : LEVELS) {
            Counter.builder(EVENTS)
                    .description("micrometer-level-默认日志记录计数器")
                    .tag("level", level)
                    .register(meterRegistry);
        }
    }
}

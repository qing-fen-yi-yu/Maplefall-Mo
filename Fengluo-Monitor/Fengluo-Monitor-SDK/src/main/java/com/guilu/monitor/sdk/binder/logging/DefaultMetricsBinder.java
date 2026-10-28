package com.guilu.monitor.sdk.binder.logging;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.logging.LogbackMetrics;
import org.springframework.lang.NonNull;

public class DefaultMetricsBinder extends AbstractLoggingBinder {

    private LogbackMetrics logbackMetrics;

    @Override
    public void bindTo(@NonNull MeterRegistry meterRegistry) {
        this.logbackMetrics = new LogbackMetrics();
        this.logbackMetrics.bindTo(meterRegistry);

        for (String level : LEVELS) {
            Counter.builder(EVENTS)
                    .description("micrometer-level-默认日志记录计数器")
                    .tag("level", level)
                    .register(meterRegistry);
        }
    }

    // 由容器在应用关闭时调用
    public void close() {
        if (logbackMetrics != null) {
            logbackMetrics.close();
        }
    }
}
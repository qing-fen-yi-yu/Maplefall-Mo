package com.guilu.monitor.sdk.binder.logging;

import io.micrometer.core.instrument.FunctionCounter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.Resource;
import org.springframework.lang.NonNull;

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicLong;

public class BasicExpandMetricsBinder extends AbstractLoggingBinder {
    @Resource
    private ExpandAppender appender;

    @Override
    public void bindTo(@NonNull MeterRegistry meterRegistry) {
        if (appender == null) {
            return;
        }
        FunctionCounter.builder("fengluo_total-max-capacity",appender, ExpandAppender::getQueueCapacity)
                        .description("总日志存储队列存储容量")
                        .register(meterRegistry);
        bindCounters(meterRegistry);
        bindEvictedByLevel(meterRegistry);
        bindQueueMetrics(meterRegistry);
    }

    private void bindCounters(MeterRegistry registry) {
        FunctionCounter
                .builder("fengluo_expand.appender.logs.received",
                        appender, a -> a.getReceivedCount().doubleValue())
                .description("日志处理事件总数")
                .register(registry);

        FunctionCounter
                .builder("fengluo_expand.appender.logs.total.offered",
                        appender, a -> a.getTotalOfferedCount().doubleValue())
                .description("日志总入队量")
                .register(registry);

        FunctionCounter
                .builder("fengluo_expand.appender.logs.total.evicted",
                        appender, a -> a.getTotalEvictedCount().doubleValue())
                .description("日志总丢弃量")
                .register(registry);
    }

    private void bindEvictedByLevel(MeterRegistry registry) {
        Map<String, AtomicLong> evictedByLevel = appender.getEvictedByLevel();
        for (String level : LEVELS) {
            AtomicLong counter = evictedByLevel.computeIfAbsent(level, k -> new AtomicLong());
            FunctionCounter
                    .builder("fengluo_expand.appender.logs.level.evicted",
                            counter, AtomicLong::doubleValue)
                    .tag("level", level)
                    .description("不同等级的日志丢弃量")
                    .register(registry);
        }
    }

    private void bindQueueMetrics(MeterRegistry registry) {
        bindQueueGauge(registry, "total", appender.getTotalLogQueue());
        bindQueueGauge(registry, "info", appender.getInfoQueue());
        bindQueueGauge(registry, "error", appender.getErrorQueue());
        bindQueueGauge(registry, "warn", appender.getWarnQueue());
        bindQueueGauge(registry, "debug", appender.getDebugQueue());
        bindQueueGauge(registry, "trace", appender.getTraceQueue());
    }

    private void bindQueueGauge(MeterRegistry registry, String queueName, BlockingQueue<?> queue) {
        Gauge.builder("fengluo_expand.appender.queue.size", queue, BlockingQueue::size)
                .tag("queue", queueName)
                .description("各日志等级的当前队列记录数")
                .register(registry);
    }

    @Override
    public void close() {
    }
}
package com.guilu.monitor.sdk.binder;

import com.guilu.jvm.GcEventMonitor;
import com.guilu.domain.GcEventInfo;
import com.guilu.monitor.sdk.constants.MetricNames;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.binder.MeterBinder;
import lombok.extern.slf4j.Slf4j;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * GC 事件指标绑定器。
 */
@Slf4j
public final class GcMetricsBinder implements MeterBinder, AutoCloseable {

    private final Map<MeterRegistry, GcEventMonitor> monitors = new IdentityHashMap<>();
    private final Map<MeterRegistry, Map<List<String>, Timer>> timers = new IdentityHashMap<>();
    private boolean closed;

    @Override
    public synchronized void bindTo(MeterRegistry registry) {
        if (closed) {
            throw new IllegalStateException("GcMetricsBinder has been closed and cannot be reused.");
        }
        if (monitors.containsKey(registry)) {
            return;
        }
        Map<List<String>, Timer> timerCache = new ConcurrentHashMap<>();
        timers.put(registry, timerCache);
        monitors.put(registry, new GcEventMonitor(event -> record(registry, timerCache, event)));
    }

    static void record(MeterRegistry registry, Map<List<String>, Timer> timerCache, GcEventInfo event) {
        if (event == null) {
            return;
        }
        try {
            List<String> key = List.of(
                    safeTag(event.collector(), "N"),
                    safeTag(event.cause(), "N"),
                    safeTag(event.action(), "N"));
            Timer timer = timerCache.computeIfAbsent(key, k -> Timer.builder(MetricNames.JVM.JVM_GC_EVENT)
                    .tags("collector", k.get(0), "cause", k.get(1), "action", k.get(2))
                    .description("JVM GC 回收器、原因及行为统计")
                    .publishPercentiles(0.5, 0.95, 0.99)
                    .register(registry));
            timer.record(event.duration(), TimeUnit.MILLISECONDS);
        } catch (RuntimeException e) {
            log.warn("GC event metric failed", e);
        }
    }

    private static String safeTag(String value, String defaultValue) {
        return value != null && !value.isEmpty() ? value : defaultValue;
    }

    @Override
    public synchronized void close() {
        if (closed) {
            return;
        }
        closed = true;
        monitors.values().forEach(GcEventMonitor::close);
        monitors.clear();
        timers.forEach((registry, cache) -> cache.values().forEach(registry::remove));
        timers.clear();
    }
}

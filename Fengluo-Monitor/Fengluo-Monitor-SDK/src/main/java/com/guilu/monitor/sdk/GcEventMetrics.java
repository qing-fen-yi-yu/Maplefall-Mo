package com.guilu.monitor.sdk;

import com.guilu.jvm.GcEventMonitor;
import com.guilu.domain.GcEventInfo;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.binder.MeterBinder;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * GC 绑定器
 */
public final class GcEventMetrics implements MeterBinder, AutoCloseable {
    private final Map<MeterRegistry, GcEventMonitor> subscriptions = new IdentityHashMap<>();
    private boolean closed;
    @Override
    public synchronized void bindTo(MeterRegistry registry) {
        if (closed) throw new IllegalStateException("GC metrics are closed");
        if (subscriptions.containsKey(registry)) return;
        subscriptions.put(registry, new GcEventMonitor(event -> record(registry, event)));
    }
    static void record(MeterRegistry registry, GcEventInfo event) {
        Timer.builder("fengluo.jvm.gc.event")
                .tags("collector", event.collector(), "cause", event.cause(), "action", event.action())
                .register(registry).record(event.duration(), TimeUnit.MILLISECONDS);
    }
    @Override
    public synchronized void close() {
        if (closed) return;
        closed = true;
        subscriptions.values().forEach(GcEventMonitor::close);
        subscriptions.clear();
    }
}

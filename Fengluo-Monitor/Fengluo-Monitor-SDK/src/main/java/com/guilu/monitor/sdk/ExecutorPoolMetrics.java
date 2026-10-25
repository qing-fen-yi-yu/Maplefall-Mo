package com.guilu.monitor.sdk;

import com.guilu.domain.DynamicPoolSnapshot;
import io.micrometer.core.instrument.*;
import io.micrometer.core.instrument.binder.MeterBinder;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.*;


public final class ExecutorPoolMetrics implements MeterBinder, AutoCloseable {
    private final Supplier<Map<String, DynamicPoolSnapshot>> source;
    private final long intervalMillis;
    private final Map<MeterRegistry, Map<String, State>> bindings = new IdentityHashMap<>();
    private ScheduledExecutorService scheduler;
    private boolean closed;
    private static final class State {
        volatile DynamicPoolSnapshot snapshot;
        final List<Meter> meters = new ArrayList<>();
        State(DynamicPoolSnapshot snapshot) { this.snapshot = snapshot; }
    }
    public ExecutorPoolMetrics(Supplier<Map<String, DynamicPoolSnapshot>> source, Duration interval) {
        this.source = Objects.requireNonNull(source, "source");
        intervalMillis = Objects.requireNonNull(interval, "interval").toMillis();
        if (intervalMillis < 1) throw new IllegalArgumentException("refresh interval must be at least 1ms");
    }
    @Override
    public synchronized void bindTo(MeterRegistry registry) {
        if (closed) throw new IllegalStateException("Executor metrics are closed");
        if (bindings.containsKey(registry)) return;
        bindings.put(registry, new HashMap<>());
        try { refresh(); }
        catch (RuntimeException e) {
            bindings.remove(registry).values().forEach(state -> state.meters.forEach(registry::remove));
            throw e;
        }
        if (scheduler == null) {
            scheduler = Executors.newSingleThreadScheduledExecutor(task -> {
                Thread thread = new Thread(task, "fengluo-executor-metrics"); thread.setDaemon(true); return thread;
            });
            scheduler.scheduleWithFixedDelay(this::refreshSafely, intervalMillis, intervalMillis, TimeUnit.MILLISECONDS);
        }
    }
    private void refreshSafely() {
        try { refresh(); } catch (RuntimeException e) {
            System.getLogger(getClass().getName()).log(System.Logger.Level.WARNING,
                    "Pool metric refresh failed", e);
        }
    }

    public synchronized void refresh() {
        if (closed || bindings.isEmpty()) return;
        Map<String, DynamicPoolSnapshot> snapshots =
                Objects.requireNonNull(source.get(), "snapshots");
        bindings.forEach((registry, states) -> {
            states.entrySet().removeIf(entry -> {
                if (snapshots.containsKey(entry.getKey())) {
                    return false;
                }
                entry.getValue().meters.forEach(registry::remove);
                return true;
            });
            snapshots.forEach((name, snapshot) -> {
                State state = states.get(name);
                if (state == null) {
                    state = new State(snapshot); states.put(name, state);
                    register(registry, name, state);
                } else {
                    state.snapshot = snapshot;
                }
            });
        });
    }
    private static void register(MeterRegistry registry, String name, State state) {
        gauge(registry, name, state, "size", DynamicPoolSnapshot::getPoolSize);
        gauge(registry, name, state, "core", DynamicPoolSnapshot::getCoreSize);
        gauge(registry, name, state, "max", DynamicPoolSnapshot::getMaxSize);
        gauge(registry, name, state, "largest", DynamicPoolSnapshot::getLargestPoolSize);
        gauge(registry, name, state, "active", DynamicPoolSnapshot::getActiveCount);
        gauge(registry, name, state, "queue.size", DynamicPoolSnapshot::getQueueSize);
        gauge(registry, name, state, "queue.remaining", DynamicPoolSnapshot::getRemainingCapacity);
        gauge(registry, name, state, "tasks", DynamicPoolSnapshot::getTaskCount);
        gauge(registry, name, state, "completed", DynamicPoolSnapshot::getCompletedTasks);
    }
    private static void gauge(MeterRegistry registry, String name, State state, String metric,
            Function<DynamicPoolSnapshot, Number> value) {
        state.meters.add(Gauge.builder("fengluo.executor." + metric, state,
                s -> {
            Number n = value.apply(s.snapshot);
            return n == null || n.doubleValue() < 0 ? Double.NaN : n.doubleValue();})
                .tag("pool", name).strongReference(true).register(registry));
    }
    @Override
    public synchronized void close() {
        if (closed) return;
        closed = true;
        if (scheduler != null) scheduler.shutdownNow();
        bindings.forEach((registry, states) -> states.values().forEach(s -> s.meters.forEach(registry::remove)));
        bindings.clear();
    }
}

package com.guilu.monitor.sdk;

import com.guilu.monitor.sdk.spi.ThreadPoolSnapshot;
import com.guilu.monitor.sdk.spi.ThreadPoolSnapshotSource;
import io.micrometer.core.instrument.FunctionCounter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.binder.MeterBinder;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;
@Slf4j
public final class ThreadPoolMetricsBinder implements MeterBinder, AutoCloseable {

    private final ThreadPoolSnapshotSource source;
    private final long intervalMillis;
    private final Map<MeterRegistry, Map<String, State>> bindings = new IdentityHashMap<>();

    private ScheduledExecutorService scheduler;
    private boolean closed;

    public ThreadPoolMetricsBinder(ThreadPoolSnapshotSource source, Duration refreshInterval) {
        this.source = Objects.requireNonNull(source, "source");
        Objects.requireNonNull(refreshInterval, "refreshInterval");
        this.intervalMillis = refreshInterval.toMillis();
        if (intervalMillis < 1L) {
            throw new IllegalArgumentException("refresh interval must be at least 1ms");
        }
    }

    @Override
    public synchronized void bindTo(MeterRegistry registry) {
        Objects.requireNonNull(registry, "registry");
        if (closed) {
            throw new IllegalStateException("Thread pool metrics are closed");
        }
        if (bindings.containsKey(registry)) {
            return;
        }
        Map<String, State> states = new HashMap<>();
        bindings.put(registry, states);
        try {
            doRefresh();
        } catch (RuntimeException e) {
            states.values().forEach(state -> removeMeters(registry, state));
            bindings.remove(registry);
            throw e;
        }
        if (scheduler == null) {
            scheduler = Executors.newSingleThreadScheduledExecutor(task -> {
                Thread thread = new Thread(task, "fengluo-executor-metrics");
                thread.setDaemon(true);
                return thread;
            });
            scheduler.scheduleWithFixedDelay(this::refreshSafely,
                    intervalMillis, intervalMillis, TimeUnit.MILLISECONDS);
        }
    }

    public void refresh() {
        doRefresh();
    }

    private void refreshSafely() {
        try {
            doRefresh();
        } catch (Exception e) {
            log.error("Pool metric refresh failed", e);
        }
    }

    private synchronized void doRefresh() {
        Map<String, ThreadPoolSnapshot> snapshotMap = source.snapshot();
        if (snapshotMap == null) {
            return;
        }
        Map<String, ThreadPoolSnapshot> snapshots = new HashMap<>(snapshotMap);
        if (closed || bindings.isEmpty()) {
            return;
        }
        bindings.forEach((registry, states) -> {
            states.entrySet().removeIf(entry -> {
                if (snapshots.get(entry.getKey()) != null) {
                    return false;
                }
                removeMeters(registry, entry.getValue());
                return true;
            });
            snapshots.forEach((name, snapshot) -> {
                if (name == null || snapshot == null) {
                    return;
                }
                State state = states.get(name);
                if (state == null) {
                    state = new State(name, snapshot);
                    states.put(name, state);
                    registerAll(registry, state);
                } else {
                    state.update(snapshot);
                    updateInfo(registry, state);
                    if (state.rejectedMeter == null && snapshot.rejectedTaskCount() >= 0) {
                        registerRejectedCounter(registry, state);
                    }
                }
            });
        });
    }

    @Override
    public synchronized void close() {
        if (closed) {
            return;
        }
        closed = true;
        if (scheduler != null) {
            scheduler.shutdownNow();
            scheduler = null;
        }
        bindings.forEach((registry, states) ->
                states.values().forEach(state -> removeMeters(registry, state)));
        bindings.clear();
    }

    private static void registerAll(MeterRegistry registry, State state) {
        gauge(registry, MetricNames.EXECUTOR.EXECUTOR_SIZE, state, ThreadPoolSnapshot::poolSize);
        gauge(registry, MetricNames.EXECUTOR.EXECUTOR_CORE, state, ThreadPoolSnapshot::coreSize);
        gauge(registry, MetricNames.EXECUTOR.EXECUTOR_MAX, state, ThreadPoolSnapshot::maxSize);
        gauge(registry, MetricNames.EXECUTOR.EXECUTOR_LARGEST, state, ThreadPoolSnapshot::largestPoolSize);
        gauge(registry, MetricNames.EXECUTOR.EXECUTOR_ACTIVE, state, ThreadPoolSnapshot::activeCount);
        gauge(registry, MetricNames.EXECUTOR.EXECUTOR_QUEUE_SIZE, state, ThreadPoolSnapshot::queueSize);
        gauge(registry, MetricNames.EXECUTOR.EXECUTOR_QUEUE_REMAINING, state,
                s -> s.hasUnboundedQueue() ? Double.NaN : s.remainingCapacity());
        gauge(registry, MetricNames.EXECUTOR.EXECUTOR_QUEUE_CAPACITY, state,
                s -> s.hasUnboundedQueue() ? Double.NaN : (double) s.queueSize() + s.remainingCapacity());
        gauge(registry, MetricNames.EXECUTOR.EXECUTOR_TASKS, state, ThreadPoolSnapshot::taskCount);
        gauge(registry, MetricNames.EXECUTOR.EXECUTOR_COMPLETED, state, ThreadPoolSnapshot::completedTaskCount);
        gauge(registry, MetricNames.EXECUTOR.EXECUTOR_KEEP_ALIVE, state,
                s -> s.keepAlive().toNanos() / 1_000_000_000d, "seconds");
        statusGauge(registry, state, "shutdown", ThreadPoolSnapshot::shutdown);
        statusGauge(registry, state, "terminating", ThreadPoolSnapshot::terminating);
        statusGauge(registry, state, "terminated", ThreadPoolSnapshot::terminated);
        updateInfo(registry, state);
        if (state.snapshot.rejectedTaskCount() >= 0) {
            registerRejectedCounter(registry, state);
        }
    }

    private static void gauge(MeterRegistry registry, String metricName, State state,
                              ToDoubleFunction<ThreadPoolSnapshot> read) {
        gauge(registry, metricName, state, read, null);
    }

    private static void gauge(MeterRegistry registry, String metricName, State state,
                              ToDoubleFunction<ThreadPoolSnapshot> read, String baseUnit) {
        var builder = Gauge.builder(metricName, state, s -> {
                    ThreadPoolSnapshot snapshot = s.snapshot;
                    return snapshot == null ? Double.NaN : read.applyAsDouble(snapshot);
                })
                .tag("pool", state.name);
        if (baseUnit != null) {
            builder.baseUnit(baseUnit);
        }
        state.meters.add(builder.register(registry));
    }

    private static void statusGauge(MeterRegistry registry, State state, String status,
                                    Predicate<ThreadPoolSnapshot> active) {
        state.meters.add(Gauge.builder(MetricNames.EXECUTOR.EXECUTOR_STATUS, state,
                        s -> s.snapshot != null && active.test(s.snapshot) ? 1d : 0d)
                .tag("pool", state.name)
                .tag("state", status)
                .register(registry));
    }

    private static void registerRejectedCounter(MeterRegistry registry, State state) {
        state.rejectedMeter = FunctionCounter.builder(
                        MetricNames.EXECUTOR.EXECUTOR_REJECTED, state, State::rejectedTotal)
                .description("任务拒绝次数")
                .tag("pool", state.name)
                .register(registry);
    }

    private static void updateInfo(MeterRegistry registry, State state) {
        List<Tag> tags = infoTags(state);
        if (tags.equals(state.infoTags)) {
            return;
        }
        if (state.infoMeter != null) {
            registry.remove(state.infoMeter);
        }
        state.infoMeter = Gauge.builder(MetricNames.EXECUTOR.EXECUTOR_INFO, state, s -> 1d)
                .tags(tags)
                .register(registry);
        state.infoTags = tags;
    }

    private static List<Tag> infoTags(State state) {
        ThreadPoolSnapshot snapshot = state.snapshot;
        return List.of(
                Tag.of("pool", state.name),
                Tag.of("queue", text(snapshot.queueType())),
                Tag.of("reject_handler", text(snapshot.rejectHandler())),
                Tag.of("thread_id", snapshot.threadId() == null
                        ? "unknown" : snapshot.threadId().toString()),
                Tag.of("thread_name", text(snapshot.threadName())),
                Tag.of("time_unit", text(snapshot.timeUnit())));
    }

    private static String text(String value) {
        return value == null || value.isBlank() ? "unknown" : value;
    }

    private static void removeMeters(MeterRegistry registry, State state) {
        state.meters.forEach(registry::remove);
        state.meters.clear();
        if (state.infoMeter != null) {
            registry.remove(state.infoMeter);
            state.infoMeter = null;
        }
        if (state.rejectedMeter != null) {
            registry.remove(state.rejectedMeter);
            state.rejectedMeter = null;
        }
    }

    private static final class State {

        private final String name;
        private volatile ThreadPoolSnapshot snapshot;
        private final List<Meter> meters = new ArrayList<>();
        private Meter infoMeter;
        private List<Tag> infoTags = List.of();
        private Meter rejectedMeter;
        private long lastRejected = -1L;
        private double rejectedOffset;

        private State(String name, ThreadPoolSnapshot snapshot) {
            this.name = name;
            update(snapshot);
        }

        private synchronized void update(ThreadPoolSnapshot snapshot) {
            this.snapshot = snapshot;
            long raw = snapshot.rejectedTaskCount();
            if (raw < 0) {
                return;
            }
            if (lastRejected < 0) {
                lastRejected = raw;
                return;
            }
            if (raw < lastRejected) {
                rejectedOffset += lastRejected;
            }
            lastRejected = raw;
        }

        private synchronized double rejectedTotal() {
            return lastRejected < 0 ? 0d : rejectedOffset + lastRejected;
        }
    }
}

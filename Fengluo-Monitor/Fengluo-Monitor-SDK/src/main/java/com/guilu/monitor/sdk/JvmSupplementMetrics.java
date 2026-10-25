package com.guilu.monitor.sdk;

import com.guilu.jvm.*;
import com.guilu.sys.SystemMetrics;
import io.micrometer.core.instrument.*;
import io.micrometer.core.instrument.binder.MeterBinder;
import java.lang.management.*;
import java.util.function.ToDoubleFunction;

/**
 * JVM 绑定器
 */
public final class JvmSupplementMetrics implements MeterBinder {
    private static double known(double value) { return value < 0 ? Double.NaN : value; }
    @Override
    public void bindTo(MeterRegistry registry) {
        Gauge.builder("fengluo.system.memory.total",
                () -> known(SystemMetrics.snapshot()
                        .totalPhysicalMemory()))
                .baseUnit("bytes").register(registry);
        Gauge.builder("fengluo.system.memory.free",

                () -> known(SystemMetrics.snapshot()
                        .freePhysicalMemory()))
                .baseUnit("bytes").register(registry);

        Gauge.builder("fengluo.process.memory.virtual",
                () -> known(SystemMetrics.snapshot()
                        .committedVirtualMemory()))
                .baseUnit("bytes").register(registry);

        Gauge.builder("fengluo.process.cpu.time", () -> {
            long ns = SystemMetrics.snapshot().processCpuTime();
            return ns < 0 ? Double.NaN : ns / 1e9;
        }).baseUnit("seconds").register(registry);

        Gauge.builder("fengluo.jvm.threads.deadlocked",
                DeadLockMetrics::getDeadlockedThreadCount)
                .register(registry);

        for (MemoryPoolMXBean pool : MemMxBeanMetrics.getMemoryMxBeans()) {
            usage(registry, pool, "peak", MemoryPoolMXBean::getPeakUsage);
            usage(registry, pool, "collection", MemoryPoolMXBean::getCollectionUsage);
            threshold(registry, pool, false);
            threshold(registry, pool, true);
        }
    }
    private static void usage(MeterRegistry registry, MemoryPoolMXBean pool, String phase,
            java.util.function.Function<MemoryPoolMXBean, MemoryUsage> reader) {
        String[] fields = {"init", "used", "committed", "max"};
        for (String field : fields) {
            Gauge.builder("fengluo.jvm.memory.pool." + phase, pool, p -> {
                if (!p.isValid()) return Double.NaN;
                MemoryUsage u = reader.apply(p);
                if (u == null) return Double.NaN;
                return
                        known(switch (field) {
                            case "init" -> u.getInit(); case "used" -> u.getUsed();
                            case "committed" -> u.getCommitted(); default -> u.getMax();
                        });}).tags("pool", pool.getName(),
                                "area", pool.getType() == MemoryType.HEAP ? "heap" : "nonheap",
                                "value", field)
                            .baseUnit("bytes")
                            .strongReference(true).register(registry);
        }
    }
    private static void threshold(MeterRegistry registry, MemoryPoolMXBean pool, boolean collection) {
        String kind = collection ? "collection" : "usage";
        gauge(registry, pool, kind, "supported",
                p -> (
                        collection ?
                                p.isCollectionUsageThresholdSupported() : p.isUsageThresholdSupported()) ? 1 : 0, null);
        gauge(registry, pool, kind, "bytes", p -> {
            if (!(collection ?
                    p.isCollectionUsageThresholdSupported() : p.isUsageThresholdSupported())) {
                return Double.NaN;
            }
            return collection ? p.getCollectionUsageThreshold() : p.getUsageThreshold();
        }, "bytes");
        gauge(registry, pool, kind, "count", p -> {
            if (!(collection ? p.isCollectionUsageThresholdSupported() : p.isUsageThresholdSupported())) {
                return Double.NaN;
            }
            return collection ? p.getCollectionUsageThresholdCount() : p.getUsageThresholdCount();
        }, null);
        gauge(registry, pool, kind, "exceeded", p -> {
            if (!(collection ?
                    p.isCollectionUsageThresholdSupported() : p.isUsageThresholdSupported())) {
                return Double.NaN;
            }
            return (collection ? p.isCollectionUsageThresholdExceeded() : p.isUsageThresholdExceeded()) ? 1 : 0;
        }, null);
    }
    private static void gauge(MeterRegistry r, MemoryPoolMXBean p, String kind, String metric,
            ToDoubleFunction<MemoryPoolMXBean> reader, String unit) {
        Gauge.builder("fengluo.jvm.memory.pool.threshold." + metric, p,
                bean -> bean.isValid() ? reader.applyAsDouble(bean) : Double.NaN)
                .tags("pool", p.getName(), "kind", kind).baseUnit(unit).strongReference(true).register(r);
    }
}

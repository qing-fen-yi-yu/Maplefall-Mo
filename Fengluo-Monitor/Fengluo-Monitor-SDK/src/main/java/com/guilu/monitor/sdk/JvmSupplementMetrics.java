package com.guilu.monitor.sdk;

import com.guilu.jvm.ClassloadingMxBeanMetrics;
import com.guilu.jvm.DeadLockMetrics;
import com.guilu.jvm.GcMxBeanMetrics;
import com.guilu.jvm.MemMxBeanMetrics;
import com.guilu.jvm.ThreadMetrics;
import com.guilu.monitor.domain.AppCompileInfo;
import com.guilu.monitor.domain.SystemDetailMetricsInfo;
import com.guilu.monitor.domain.SystemMetricsInfo;
import com.guilu.monitor.sdk.support.CachedSnapshot;
import com.guilu.sys.SystemMetrics;
import io.micrometer.core.instrument.FunctionCounter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;
import jakarta.annotation.Nonnull;

import java.lang.management.BufferPoolMXBean;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryType;
import java.lang.management.MemoryUsage;
import java.time.Duration;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;
import java.util.function.ToDoubleFunction;

import static com.guilu.MXBean.getMemoryBean;

/**
 * JVM 绑定器。
 *
 * <p>系统与死锁数据通过 {@link CachedSnapshot} 按 TTL 缓存，避免每次
 * Prometheus scrape 都重复调用操作系统接口或抓取线程栈。</p>
 */
public final class JvmSupplementMetrics implements MeterBinder {

    private final CachedSnapshot<SystemMetricsInfo> systemCache;
    private final CachedSnapshot<Integer> deadlockCache;
    private final CachedSnapshot<SystemDetailMetricsInfo> detailCache;
    private final Set<MeterRegistry> bound =
            Collections.newSetFromMap(new IdentityHashMap<>());

    public JvmSupplementMetrics() {
        this(Duration.ofSeconds(10), false);
    }

    public JvmSupplementMetrics(Duration refreshInterval, boolean detailEnabled) {
        this(refreshInterval, detailEnabled, SystemMetrics::snapshot, null);
    }

    JvmSupplementMetrics(Duration refreshInterval, boolean detailEnabled,
                         Supplier<SystemMetricsInfo> systemSupplier,
                         Supplier<SystemDetailMetricsInfo> detailSupplier) {
        Objects.requireNonNull(refreshInterval, "refreshInterval");
        Objects.requireNonNull(systemSupplier, "systemSupplier");
        this.systemCache = new CachedSnapshot<>(systemSupplier, refreshInterval);
        this.deadlockCache = new CachedSnapshot<>(DeadLockMetrics::getDeadlockedThreadCount, refreshInterval);
        this.detailCache = detailEnabled
                ? new CachedSnapshot<>(detailSupplier != null
                        ? detailSupplier : SystemMetrics::detailInfoSnapshot, refreshInterval)
                : null;
    }

    @Override
    public synchronized void bindTo(@Nonnull MeterRegistry registry) {
        if (!bound.add(registry)) {
            return;
        }

        systemGauge(registry, "fengluo.system.memory.total", SystemMetricsInfo::totalPhysicalMemory,
                "bytes", "系统内存总量");
        systemGauge(registry, "fengluo.system.memory.free", SystemMetricsInfo::freePhysicalMemory,
                "bytes", "空闲内存量");
        systemGauge(registry, "fengluo.process.memory.virtual", SystemMetricsInfo::committedVirtualMemory,
                "bytes", "进程可使用的虚拟内存量");
        systemGauge(registry, MetricNames.SYSTEM_CPU_LOAD, SystemMetricsInfo::systemCpuLoad,
                null, "系统CPU使用率");
        systemGauge(registry, MetricNames.PROCESS_CPU_LOAD, SystemMetricsInfo::processCpuLoad,
                null, "JVM进程CPU使用率");
        systemGauge(registry, MetricNames.SYSTEM_LOAD_AVERAGE, SystemMetricsInfo::systemLoadAverage,
                null, "系统最近1分钟平均负载");
        systemGauge(registry, MetricNames.SYSTEM_SWAP_TOTAL, SystemMetricsInfo::totalSwapSpace,
                "bytes", "Swap空间总量");
        systemGauge(registry, MetricNames.SYSTEM_SWAP_FREE, SystemMetricsInfo::freeSwapSpace,
                "bytes", "空闲Swap空间");
        systemGauge(registry, MetricNames.PROCESS_FD_OPEN, SystemMetricsInfo::openFileDescriptorCount,
                null, "进程打开的文件描述符数量");
        systemGauge(registry, MetricNames.PROCESS_FD_MAX, SystemMetricsInfo::maxFileDescriptorCount,
                null, "进程可打开的文件描述符上限");

        FunctionCounter.builder("fengluo.process.cpu.time", systemCache, cache -> {
                    SystemMetricsInfo snapshot = cache.get();
                    if (snapshot == null) {
                        return 0d;
                    }
                    long nanos = snapshot.processCpuTime();
                    return nanos < 0 ? 0d : nanos / 1_000_000_000d;
                })
                .description("JVM进程累计CPU时间")
                .baseUnit("seconds")
                .register(registry);

        gauge(registry, MetricNames.JVM_THREADS_DEADLOCKED,
                () -> {
                    Integer value = deadlockCache.get();
                    return value == null ? Double.NaN : value.doubleValue();
                }, null, "线程死锁数量");
        gauge(registry, MetricNames.JVM_THREADS_COUNT,
                () -> (double) ThreadMetrics.getThreadCount(), null, "当前线程数量");
        gauge(registry, MetricNames.JVM_THREADS_DAEMON,
                () -> (double) ThreadMetrics.getDaemonThreadCount(), null, "守护线程数量");
        gauge(registry, MetricNames.JVM_THREADS_PEAK,
                () -> (double) ThreadMetrics.getPeakThreadCount(), null, "峰值线程数量");
        gauge(registry, MetricNames.JVM_THREADS_STARTED,
                () -> (double) ThreadMetrics.getTotalStartedThreadCount(), null, "累计启动线程数量");

        gauge(registry, MetricNames.JVM_CLASSES_LOADED,
                () -> ClassloadingMxBeanMetrics.getLoadedClassCount().doubleValue(), null, "当前加载类数量");
        gauge(registry, MetricNames.JVM_CLASSES_UNLOADED,
                () -> ClassloadingMxBeanMetrics.unLoadedClassCount().doubleValue(), null, "累计卸载类数量");

        MemoryMXBean memoryBean = getMemoryBean();
        memoryGauge(registry, memoryBean, true, "used");
        memoryGauge(registry, memoryBean, true, "committed");
        memoryGauge(registry, memoryBean, true, "max");
        memoryGauge(registry, memoryBean, false, "used");
        memoryGauge(registry, memoryBean, false, "committed");
        memoryGauge(registry, memoryBean, false, "max");
        gauge(registry, MetricNames.JVM_MEMORY_PENDING_FINALIZATION,
                () -> (double) memoryBean.getObjectPendingFinalizationCount(), null, "待终结对象数量");

        for (GarbageCollectorMXBean bean : GcMxBeanMetrics.getGcMxBeans()) {
            Gauge.builder(MetricNames.JVM_GC_COUNT, bean,
                            b -> b.getCollectionCount() < 0 ? Double.NaN : b.getCollectionCount())
                    .tag("collector", bean.getName())
                    .description("GC累计次数")
                    .register(registry);
            Gauge.builder(MetricNames.JVM_GC_TIME, bean,
                            b -> b.getCollectionTime() < 0
                                    ? Double.NaN : b.getCollectionTime() / 1000d)
                    .tag("collector", bean.getName())
                    .baseUnit("seconds")
                    .description("GC累计耗时")
                    .register(registry);
        }

        for (BufferPoolMXBean bean : MemMxBeanMetrics.getLocalBufferMxBeans()) {
            Gauge.builder(MetricNames.JVM_BUFFER_COUNT, bean, BufferPoolMXBean::getCount)
                    .tag("pool", bean.getName())
                    .description("缓冲区数量")
                    .register(registry);
            Gauge.builder(MetricNames.JVM_BUFFER_MEMORY_USED, bean, b -> {
                        long used = b.getMemoryUsed();
                        return used < 0 ? Double.NaN : used;
                    })
                    .tag("pool", bean.getName())
                    .baseUnit("bytes")
                    .description("缓冲区内存占用")
                    .register(registry);
        }

        AppCompileInfo compileInfo = AppCompileInfo.of();
        if (compileInfo.getJitName() != null) {
            gauge(registry, MetricNames.JVM_COMPILATION_TIME,
                    () -> compileInfo.getTotalCompileTime() / 1000d,
                    "seconds", "JIT累计编译耗时");
        }

        for (MemoryPoolMXBean pool : MemMxBeanMetrics.getMemoryMxBeans()) {
            usage(registry, pool, "peak", MemoryPoolMXBean::getPeakUsage);
            usage(registry, pool, "collection", MemoryPoolMXBean::getCollectionUsage);
            threshold(registry, pool, false);
            threshold(registry, pool, true);
        }

        if (detailCache != null) {
            registerDetailMetrics(registry);
        }
    }

    private void registerDetailMetrics(MeterRegistry registry) {
        detailGauge(registry, MetricNames.SYSTEM_CPU_COUNT,
                d -> num(d.getCpuCount()), null, "CPU核数");
        detailGauge(registry, MetricNames.SYSTEM_CPU_USER,
                d -> num(d.getCpuUserUsed()), null, "CPU用户态占用率");
        detailGauge(registry, MetricNames.SYSTEM_CPU_SYSTEM,
                d -> num(d.getCpuSysUsed()), null, "CPU系统态占用率");
        detailGauge(registry, MetricNames.SYSTEM_CPU_IDLE,
                d -> num(d.getCpuIdle()), null, "CPU空闲率");
        detailGauge(registry, MetricNames.SYSTEM_CPU_WAIT,
                d -> num(d.getCpuWait()), null, "CPU等待率");
        detailGauge(registry, MetricNames.SYSTEM_CPU_TOTAL,
                d -> num(d.getCpuTotalUsed()), null, "CPU总使用率");
        detailGauge(registry, MetricNames.SYSTEM_CPU_WAIT_TIME,
                d -> num(d.getCpuWaitTime()), null, "CPU累计等待tick");
        detailGauge(registry, MetricNames.SYSTEM_MEMORY_USED,
                d -> num(d.getMemUsed()), "bytes", "物理内存已使用量");
        detailGauge(registry, MetricNames.SYSTEM_MEMORY_USAGE,
                d -> num(d.getMemRate()), null, "物理内存占用率");
        detailGauge(registry, MetricNames.JVM_MEMORY_RUNTIME_TOTAL,
                d -> num(d.getJvmTotal()), "bytes", "JVM已申请内存");
        detailGauge(registry, MetricNames.JVM_MEMORY_RUNTIME_FREE,
                d -> num(d.getJvmFree()), "bytes", "JVM空闲内存");
    }

    private static double num(Number value) {
        return value == null ? Double.NaN : value.doubleValue();
    }

    private void systemGauge(MeterRegistry registry, String name,
                             ToDoubleFunction<SystemMetricsInfo> read,
                             String unit, String description) {
        gauge(registry, name, () -> {
            SystemMetricsInfo snapshot = systemCache.get();
            if (snapshot == null) {
                return Double.NaN;
            }
            double value = read.applyAsDouble(snapshot);
            return value < 0 ? Double.NaN : value;
        }, unit, description);
    }

    private void detailGauge(MeterRegistry registry, String name,
                             ToDoubleFunction<SystemDetailMetricsInfo> read,
                             String unit, String description) {
        gauge(registry, name, () -> {
            SystemDetailMetricsInfo detail = detailCache.get();
            return detail == null ? Double.NaN : read.applyAsDouble(detail);
        }, unit, description);
    }

    private static void gauge(MeterRegistry registry, String name,
                              Supplier<Double> reader, String unit, String description) {
        var builder = Gauge.builder(name, reader, source -> {
                    Double value = source.get();
                    return value == null ? Double.NaN : value;
                })
                .strongReference(true);
        if (unit != null) {
            builder.baseUnit(unit);
        }
        if (description != null) {
            builder.description(description);
        }
        builder.register(registry);
    }

    private static void memoryGauge(MeterRegistry registry, MemoryMXBean bean,
                                    boolean heap, String field) {
        Gauge.builder("fengluo.jvm.memory." + field, bean, b -> {
                    MemoryUsage usage = heap ? b.getHeapMemoryUsage() : b.getNonHeapMemoryUsage();
                    if (usage == null) {
                        return Double.NaN;
                    }
                    long value = switch (field) {
                        case "used" -> usage.getUsed();
                        case "committed" -> usage.getCommitted();
                        default -> usage.getMax();
                    };
                    return value < 0 ? Double.NaN : value;
                })
                .tags("area", heap ? "heap" : "nonheap")
                .baseUnit("bytes")
                .register(registry);
    }

    private static void usage(MeterRegistry registry, MemoryPoolMXBean pool, String phase,
                              java.util.function.Function<MemoryPoolMXBean, MemoryUsage> reader) {
        String[] fields = {"init", "used", "committed", "max"};
        for (String field : fields) {
            Gauge.builder("fengluo.jvm.memory.pool", pool, p -> {
                        if (!p.isValid()) {
                            return Double.NaN;
                        }
                        MemoryUsage usage = reader.apply(p);
                        if (usage == null) {
                            return Double.NaN;
                        }
                        long value = switch (field) {
                            case "init" -> usage.getInit();
                            case "used" -> usage.getUsed();
                            case "committed" -> usage.getCommitted();
                            default -> usage.getMax();
                        };
                        return value < 0 ? Double.NaN : value;
                    })
                    .tags("pool", pool.getName(),
                            "area", pool.getType() == MemoryType.HEAP ? "heap" : "nonheap",
                            "phase", phase,
                            "value", field)
                    .baseUnit("bytes")
                    .description("JVM内存池占用指标")
                    .register(registry);
        }
    }

    private static void threshold(MeterRegistry registry, MemoryPoolMXBean pool, boolean collection) {
        String kind = collection ? "collection" : "usage";

        java.util.function.Predicate<MemoryPoolMXBean> isSupported =
                p -> collection ? p.isCollectionUsageThresholdSupported() : p.isUsageThresholdSupported();

        thresholdGauge(registry, pool, kind, "supported",
                p -> isSupported.test(p) ? 1.0 : 0.0, null);
        thresholdGauge(registry, pool, kind, "bytes", p -> {
            if (!isSupported.test(p)) {
                return Double.NaN;
            }
            return collection ? p.getCollectionUsageThreshold() : p.getUsageThreshold();
        }, "bytes");
        thresholdGauge(registry, pool, kind, "count", p -> {
            if (!isSupported.test(p)) {
                return Double.NaN;
            }
            return collection ? p.getCollectionUsageThresholdCount() : p.getUsageThresholdCount();
        }, null);
        thresholdGauge(registry, pool, kind, "exceeded", p -> {
            if (!isSupported.test(p)) {
                return Double.NaN;
            }
            return (collection ? p.isCollectionUsageThresholdExceeded()
                    : p.isUsageThresholdExceeded()) ? 1.0 : 0.0;
        }, null);
    }

    private static void thresholdGauge(MeterRegistry registry, MemoryPoolMXBean pool,
                                       String kind, String metric,
                                       ToDoubleFunction<MemoryPoolMXBean> reader, String unit) {
        var builder = Gauge.builder("fengluo.jvm.memory.pool.threshold." + metric, pool,
                        bean -> bean.isValid() ? reader.applyAsDouble(bean) : Double.NaN)
                .tags("pool", pool.getName(), "kind", kind);
        if (unit != null) {
            builder.baseUnit(unit);
        }
        builder.register(registry);
    }
}

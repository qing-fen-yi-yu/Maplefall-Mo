package com.guilu.sys;

import com.guilu.domain.SystemDetailMetricsInfo;
import com.guilu.domain.SystemMetricsInfo;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.GlobalMemory;
import oshi.hardware.HardwareAbstractionLayer;

/**
 * OSHI 系统明细采集器：CPU 分项占用率、累计 iowait、物理内存以及 JVM 运行时内存。
 *
 * <p>OSHI 在部分平台上不可用；此时仍返回对象，只是明细字段为空。CPU 占用率需要
 * 两次采样的 tick 差值，第一次调用返回 {@link Double#NaN}。</p>
 */
public final class SystemDetailMetrics {

    private static final System.Logger LOG =
            System.getLogger(SystemDetailMetrics.class.getName());

    private final CentralProcessor processor;
    private final GlobalMemory memory;

    private long[] previousTicks = new long[0];

    public SystemDetailMetrics() {
        CentralProcessor cpu = null;
        GlobalMemory mem = null;
        try {
            HardwareAbstractionLayer hardware = new SystemInfo().getHardware();
            cpu = hardware.getProcessor();
            mem = hardware.getMemory();
        } catch (RuntimeException | LinkageError e) {
            LOG.log(System.Logger.Level.WARNING,
                    "OSHI unavailable; system detail metrics will be partial", e);
        }
        this.processor = cpu;
        this.memory = mem;
    }

    public synchronized SystemDetailMetricsInfo snapshot(SystemMetricsInfo base) {
        SystemMetricsInfo source = base == null ? SystemMetrics.snapshot() : base;
        Runtime runtime = Runtime.getRuntime();
        SystemDetailMetricsInfo.SystemDetailMetricsInfoBuilder builder = SystemDetailMetricsInfo.builder()
                .systemMetricsInfo(source)
                .cpuCount(processor != null
                        ? processor.getLogicalProcessorCount()
                        : runtime.availableProcessors())
                .jvmTotal(runtime.totalMemory())
                .jvmFree(runtime.freeMemory());

        if (processor != null) {
            long[] ticks = processor.getSystemCpuLoadTicks();
            builder.cpuWaitTime(tick(ticks, CentralProcessor.TickType.IOWAIT));
            if (previousTicks.length == ticks.length && ticks.length > 0) {
                long user = delta(ticks, previousTicks, CentralProcessor.TickType.USER);
                long system = delta(ticks, previousTicks, CentralProcessor.TickType.SYSTEM);
                long idle = delta(ticks, previousTicks, CentralProcessor.TickType.IDLE);
                long wait = delta(ticks, previousTicks, CentralProcessor.TickType.IOWAIT);
                long total = totalDelta(ticks, previousTicks);
                if (total > 0) {
                    builder.cpuUserUsed((double) user / total)
                            .cpuSysUsed((double) system / total)
                            .cpuIdle((double) idle / total)
                            .cpuWait((double) wait / total)
                            .cpuTotalUsed(1d - (double) idle / total);
                } else {
                    builder.cpuUserUsed(Double.NaN)
                            .cpuSysUsed(Double.NaN)
                            .cpuIdle(Double.NaN)
                            .cpuWait(Double.NaN)
                            .cpuTotalUsed(Double.NaN);
                }
            } else {
                builder.cpuUserUsed(Double.NaN)
                        .cpuSysUsed(Double.NaN)
                        .cpuIdle(Double.NaN)
                        .cpuWait(Double.NaN)
                        .cpuTotalUsed(Double.NaN);
            }
            previousTicks = ticks.clone();
        }

        if (memory != null) {
            long total = memory.getTotal();
            long used = Math.max(0L, total - memory.getAvailable());
            builder.memUsed(used)
                    .memRate(total > 0 ? (double) used / total : Double.NaN);
        }
        return builder.build();
    }

    private static long tick(long[] ticks, CentralProcessor.TickType type) {
        int index = type.getIndex();
        return index < ticks.length ? ticks[index] : 0L;
    }

    private static long delta(long[] ticks, long[] previous, CentralProcessor.TickType type) {
        int index = type.getIndex();
        if (index >= ticks.length || index >= previous.length) {
            return 0L;
        }
        return Math.max(0L, ticks[index] - previous[index]);
    }

    private static long totalDelta(long[] ticks, long[] previous) {
        long total = 0L;
        int length = Math.min(ticks.length, previous.length);
        for (int i = 0; i < length; i++) {
            total += Math.max(0L, ticks[i] - previous[i]);
        }
        return total;
    }
}

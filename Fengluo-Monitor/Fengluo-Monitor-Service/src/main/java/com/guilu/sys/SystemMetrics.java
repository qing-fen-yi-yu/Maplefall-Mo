package com.guilu.sys;
import com.guilu.MXBean;
import com.guilu.monitor.domain.SystemDetailMetricsInfo;
import com.guilu.monitor.domain.SystemMetricsInfo;
import java.lang.management.OperatingSystemMXBean;
//系统指标监听器
public final class SystemMetrics {
    private static final SystemDetailMetrics SYSTEM_DETAIL = new SystemDetailMetrics();

    private SystemMetrics() {}
    public static SystemMetricsInfo snapshot() {
        OperatingSystemMXBean bean = MXBean.getOperatingSystemBean();
        com.sun.management.OperatingSystemMXBean os = bean instanceof com.sun.management.OperatingSystemMXBean
                ? (com.sun.management.OperatingSystemMXBean) bean : null;
        com.sun.management.UnixOperatingSystemMXBean unix =
                bean instanceof com.sun.management.UnixOperatingSystemMXBean ?
                        (com.sun.management.UnixOperatingSystemMXBean) bean : null;
        return new SystemMetricsInfo(
                bean.getName(), bean.getArch(), bean.getVersion(), bean.getAvailableProcessors(),
                bean.getSystemLoadAverage(), os == null ? -1 : os.getCpuLoad(),
                os == null ? -1 : os.getProcessCpuLoad(), os == null ? -1 : os.getProcessCpuTime(),
                os == null ? -1 : os.getTotalMemorySize(), os == null ? -1 : os.getFreeMemorySize(),
                os == null ? -1 : os.getCommittedVirtualMemorySize(), os == null ? -1 : os.getTotalSwapSpaceSize(),
                os == null ? -1 : os.getFreeSwapSpaceSize(), unix == null ? -1 : unix.getOpenFileDescriptorCount(),
                unix == null ? -1 : unix.getMaxFileDescriptorCount());
    }

    public static SystemDetailMetricsInfo detailInfoSnapshot() {
        return SYSTEM_DETAIL.snapshot(snapshot());
    }
}

package com.guilu.jvm;
import com.guilu.MXBean;
import com.guilu.monitor.domain.ThreadMetricsInfo;
import java.lang.management.*;
import java.util.*;

// 线程指标器
public final class ThreadMetrics {
    private static final ThreadMXBean BEAN = MXBean.getThreadBean();
    private ThreadMetrics() {}
    public static int getThreadCount() { 
        return BEAN.getThreadCount(); 
    }
    public static int getPeakThreadCount() {
         return BEAN.getPeakThreadCount(); 
    }
    public static int getDaemonThreadCount() { 
        return BEAN.getDaemonThreadCount();
    }
    public static long getTotalStartedThreadCount() { 
        return BEAN.getTotalStartedThreadCount();
    }
    public static boolean isThreadCpuTimeSupported() { 
        return BEAN.isThreadCpuTimeSupported(); 
    }
    public static boolean isThreadCpuTimeEnabled() { 
        return isThreadCpuTimeSupported() && BEAN.isThreadCpuTimeEnabled(); 
    }
    public static boolean isThreadContentionMonitoringSupported() { 
        return BEAN.isThreadContentionMonitoringSupported(); 
    }
    public static boolean isThreadContentionMonitoringEnabled() {
        return isThreadContentionMonitoringSupported()
         && BEAN.isThreadContentionMonitoringEnabled();
    }
    public static boolean setThreadCpuTimeEnabled(boolean enabled) {
        if (!isThreadCpuTimeSupported()) return false;
        BEAN.setThreadCpuTimeEnabled(enabled); return true;
    }
    public static boolean setThreadContentionMonitoringEnabled(boolean enabled) {
        if (!isThreadContentionMonitoringSupported()) return false;
        BEAN.setThreadContentionMonitoringEnabled(enabled); return true;
    }
    public static List<ThreadMetricsInfo> getThreadMetrics(int maxDepth) {
        if (maxDepth < 0) throw new IllegalArgumentException("maxDepth must be non-negative");
        List<ThreadMetricsInfo> result = new ArrayList<>();
        boolean enabled = isThreadCpuTimeEnabled();
        for (ThreadInfo info : BEAN.getThreadInfo(BEAN.getAllThreadIds(), maxDepth)) {
            if (info != null) result.add(new ThreadMetricsInfo(info,
                    enabled ? BEAN.getThreadCpuTime(info.getThreadId()) : -1,
                    enabled ? BEAN.getThreadUserTime(info.getThreadId()) : -1));
        }
        return List.copyOf(result);
    }
    public static Map<Thread.State, Long> getThreadStateCounts() {
        Map<Thread.State, Long> result = new EnumMap<>(Thread.State.class);
        for (Thread.State state : Thread.State.values()) result.put(state, 0L);
        for (ThreadInfo info : BEAN.getThreadInfo(BEAN.getAllThreadIds()))
            if (info != null) result.merge(info.getThreadState(), 1L, Long::sum);
        return Collections.unmodifiableMap(result);
    }
}

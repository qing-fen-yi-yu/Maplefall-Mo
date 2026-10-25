package com.guilu.jvm;
import com.guilu.MXBean;
import java.lang.management.*;
import java.util.*;

/**
 * 死锁检测器
 */
public final class DeadLockMetrics {
    private DeadLockMetrics() {}
    public static long[] getDeadlockedThreadIds() {
        ThreadMXBean bean = MXBean.getThreadBean();
        long[] ids = bean.isSynchronizerUsageSupported() 
            ? bean.findDeadlockedThreads() : bean.findMonitorDeadlockedThreads();
        return ids == null ? new long[0] : ids;
    }
    public static int getDeadlockedThreadCount() { 
        return getDeadlockedThreadIds().length;
    }
    public static List<ThreadInfo> getDeadlockedThreads() {
        ThreadMXBean bean = MXBean.getThreadBean();
        long[] ids = getDeadlockedThreadIds();
        if (ids.length == 0) return List.of();
        return Arrays.stream(bean.getThreadInfo(ids, bean.isObjectMonitorUsageSupported(),
                bean.isSynchronizerUsageSupported())).filter(Objects::nonNull).toList();
    }
}

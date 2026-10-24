package com.guilu.jvm;

import com.guilu.MXBean;
import com.guilu.domain.GcInfo;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.MemoryManagerMXBean;
import java.util.*;

public final class GcMxBeanMetrics {

    private GcMxBeanMetrics() {}

    public static List<GarbageCollectorMXBean> getGcMxBeans() {
        return MXBean.getGarbageCollectorMXBeans();
    }

    public static Map<GarbageCollectorMXBean, GcInfo> getAllLocalValidGcInfo() {
        return toGcInfoMap(getGcMxBeans());
    }

    public static Map<GarbageCollectorMXBean, GcInfo> toGcInfoMap(List<GarbageCollectorMXBean> beans) {
        Objects.requireNonNull(beans, "beans");
        Map<GarbageCollectorMXBean, GcInfo> result =
                new TreeMap<>(Comparator.comparing(MemoryManagerMXBean::getName));
        for (GarbageCollectorMXBean bean : beans) {
            if (!bean.isValid()) continue;
            long count = normalizeNonNegative(bean.getCollectionCount());
            long time  = normalizeNonNegative(bean.getCollectionTime());
            String[] pools = bean.getMemoryPoolNames().clone();
            result.put(bean, new GcInfo(count, time, pools, bean.getName()));
        }
        return result;
    }

    private static long normalizeNonNegative(long v) {
        return v < 0 ? 0L : v;
    }
}

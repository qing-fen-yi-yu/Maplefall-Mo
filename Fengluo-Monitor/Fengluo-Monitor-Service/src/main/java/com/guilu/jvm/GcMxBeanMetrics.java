package com.guilu.jvm;
import com.guilu.MXBean;
import com.guilu.monitor.domain.GcInfo;
import java.lang.management.GarbageCollectorMXBean;
import java.util.*;


public final class GcMxBeanMetrics {
    private GcMxBeanMetrics() {}
    public static List<GarbageCollectorMXBean> getGcMxBeans() { return MXBean.getGarbageCollectorMXBeans(); }
    public static Map<GarbageCollectorMXBean, GcInfo> getAllLocalValidGcInfo() { return toGcInfoMap(getGcMxBeans()); }

    public static Map<GarbageCollectorMXBean, GcInfo> toGcInfoMap(List<GarbageCollectorMXBean> beans) {
        Objects.requireNonNull(beans, "beans");
        Map<GarbageCollectorMXBean, GcInfo> result = new TreeMap<>(Comparator.comparing(GarbageCollectorMXBean::getName));
        for (GarbageCollectorMXBean bean : beans) {
            if (!bean.isValid()) continue;
            com.sun.management.GcInfo last = lastInfo(bean);
            result.put(bean, new GcInfo(bean.getCollectionCount(), bean.getCollectionTime(),
                    bean.getMemoryPoolNames(), bean.getName(), last == null ? null : last.getStartTime(), null));
        }
        return Collections.unmodifiableMap(result);
    }

    /**
     * 获取上一次执行的GC参数记录
     */
    public static GcInfo getLastGc() {
        GarbageCollectorMXBean selected = null;
        com.sun.management.GcInfo latest = null;
        for (GarbageCollectorMXBean bean : getGcMxBeans()) {
            if (!bean.isValid()) continue;
            com.sun.management.GcInfo info = lastInfo(bean);
            if (info != null && (latest == null || info.getEndTime() > latest.getEndTime())) {
                selected = bean; latest = info;
            }
        }
        return selected == null ? null : new GcInfo(selected.getCollectionCount(), selected.getCollectionTime(),
                selected.getMemoryPoolNames(), selected.getName(), latest.getStartTime(), null);
    }
    private static com.sun.management.GcInfo lastInfo(GarbageCollectorMXBean bean) {
        return bean instanceof com.sun.management.GarbageCollectorMXBean hotSpot ? hotSpot.getLastGcInfo() : null;
    }
}

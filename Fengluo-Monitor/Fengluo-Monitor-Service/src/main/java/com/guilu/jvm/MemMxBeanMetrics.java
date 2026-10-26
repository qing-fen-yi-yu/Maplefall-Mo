package com.guilu.jvm;

import com.guilu.MXBean;
import com.guilu.monitor.domain.BufferPoolInfo;
import com.guilu.monitor.domain.MemoryPoolInfo;
import com.guilu.monitor.domain.MemoryPoolInfo.ThresholdInfo;

import java.lang.management.BufferPoolMXBean;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryUsage;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MemMxBeanMetrics {
    private static final MemoryMXBean memoryMXBean = MXBean.getMemoryBean();
    public static List<BufferPoolMXBean> getLocalBufferMxBeans(){
        return MXBean.getLocalAllTypeMxBean(BufferPoolMXBean.class);
    }
    public static Map<BufferPoolMXBean, BufferPoolInfo> getAllBufferInfo(){
        Map<BufferPoolMXBean,BufferPoolInfo> bpInfos = new HashMap<>();
        for (BufferPoolMXBean bufferMxBean : getLocalBufferMxBeans()) {
            bpInfos.put(bufferMxBean,
                    new BufferPoolInfo(bufferMxBean.getName(),
                            bufferMxBean.getCount(),
                            bufferMxBean.getTotalCapacity(),
                            bufferMxBean.getMemoryUsed())
                    );
        }
        return bpInfos;
    }
    /**
     * 获取本地内存池 MXBean。指标快照通过 {@link #getMemoryPoolInfo()} 读取。
     */
    public static List<MemoryPoolMXBean> getMemoryMxBeans(){
        return  MXBean.getMemoryPoolMXBeans();
    }
    //返回堆与非堆内存的使用情况
    public static MemoryUsage getHeapMemoryUsage(){
        return memoryMXBean.getHeapMemoryUsage();
    }
    public static MemoryUsage getNonHeapMemoryUsage(){
        return  memoryMXBean.getNonHeapMemoryUsage();
    }
    //未进行终结处理的对象的数量
    public static Integer getPendingCount(){
        return memoryMXBean.getObjectPendingFinalizationCount();
    }
    /**
     * 按内存池名称返回当前、峰值、最近 GC 后用量及两类阈值统计。
     */
    public static Map<String, MemoryPoolInfo> getMemoryPoolInfo() {
        Map<String, MemoryPoolInfo> result = new java.util.LinkedHashMap<>();
        for (MemoryPoolMXBean bean : getMemoryMxBeans()) {
            if (bean.isValid()) result.put(bean.getName(), snapshot(bean));
        }
        return java.util.Collections.unmodifiableMap(result);
    }
    public static MemoryPoolInfo snapshot(MemoryPoolMXBean bean) {
        java.util.Objects.requireNonNull(bean, "bean");
        ThresholdInfo usageThreshold = bean.isUsageThresholdSupported()
                ? new ThresholdInfo(true, bean.getUsageThreshold(), bean.getUsageThresholdCount(),
                        bean.isUsageThresholdExceeded()) : ThresholdInfo.unsupported();
        ThresholdInfo collectionThreshold = bean.isCollectionUsageThresholdSupported()
                ? new ThresholdInfo(true, bean.getCollectionUsageThreshold(), bean.getCollectionUsageThresholdCount(),
                        bean.isCollectionUsageThresholdExceeded()) : ThresholdInfo.unsupported();
        return new MemoryPoolInfo(bean.getName(), bean.getType(), bean.getUsage(), bean.getPeakUsage(),
                bean.getCollectionUsage(), java.util.Arrays.asList(bean.getMemoryManagerNames()),
                usageThreshold, collectionThreshold);
    }
}

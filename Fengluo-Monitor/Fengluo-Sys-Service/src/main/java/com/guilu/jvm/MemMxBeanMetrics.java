package com.guilu.jvm;

import com.guilu.MXBean;
import com.guilu.domain.BufferPoolInfo;

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
     * todo MemoryPoolMXBean
     * {@link <a href="https://docs.oracle.com/en/java/javase/17/docs/api/java.management/java/lang/management/MemoryPoolMXBean.html">...</a>}
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
}
package com.guilu.threadPool;

import io.github.mweirauch.micrometer.jvm.extras.ProcessMemoryMetrics;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import java.util.HashMap;
import java.util.Map;

@AutoConfiguration
@ConditionalOnClass(ProcessMemoryMetrics.class)
@ConditionalOnProperty(prefix = "monitor.threadpool.dynamic", name = "enabled",
        havingValue = "true", matchIfMissing = true)
public class PoolManager {
    //建立动态线程池注册表--在应用启动过程中可能还会添加
    private static volatile Map<Long, DynamicPool> map = new HashMap<>();

    public static boolean registerThreadPool(Long Id,DynamicPool pool){
        map.put(Id,pool);
        return true;
    }
    public static boolean registerThreadPool(DynamicPool pool){
        registerThreadPool(1L,pool);
        return true;
    }

    public static DynamicPool toDynamicPool(Object value) {
//        DynamicPool dynamicPool = new DynamicPool();
        return null;
    }
    public static DynamicPool toDynamicPool(Object vale,String name){
//        DynamicPool dynamicPool = new DynamicPool();
        return  null;
    }
}

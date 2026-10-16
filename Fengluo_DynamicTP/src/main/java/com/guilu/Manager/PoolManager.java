package com.guilu.Manager;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;

public class PoolManager {
    static final Map<String, Executor> POOL_REGISTRY = new ConcurrentHashMap<>();
    static final Map<String,Executor> ALIVE_REGISTRY = new ConcurrentHashMap<>();
    //累计注册次数
    static final AtomicInteger COUNT = new AtomicInteger();
    public int getRegisPoolCount() {
        return COUNT.get();
    }

    public int getCurCount() {
        return POOL_REGISTRY.size();
    }
    //获取指定线程池
    public static Executor getPoolByAll(String name) {
        return POOL_REGISTRY.get(name);
    }
    public static Executor getPoolByAlive(String name){
        return  ALIVE_REGISTRY.get(name);
    }
    public static Executor addRegistry(String poolName,Object executor){
        Executor pool =  (Executor) executor;
        ALIVE_REGISTRY.putIfAbsent(poolName , pool);
        return POOL_REGISTRY.putIfAbsent(poolName, pool);
    }
    public static void incrementCount(){
        COUNT.getAndIncrement();
    }

    public static void removeExecutorByAlive(String poolName){
        ALIVE_REGISTRY.remove(poolName);
    }
}

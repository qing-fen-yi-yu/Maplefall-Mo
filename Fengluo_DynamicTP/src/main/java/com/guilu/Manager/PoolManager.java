package com.guilu.Manager;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

public class PoolManager {
    static final Map<String, Executor> POOL_REGISTRY = new ConcurrentHashMap<>();
    static final Map<String,Executor> ALIVE_REGISTRY = new ConcurrentHashMap<>();
    //累计注册次数
    static final AtomicInteger COUNT = new AtomicInteger();
    protected Map<String,Executor> getAlivePool(){
        return ALIVE_REGISTRY;
    }
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
    //这里只进行执行更改任务，其他交由调用者决定
    public static boolean setCount(int count){
        return COUNT.compareAndSet(COUNT.get(), count);
    }
    protected void removePoolByRegis(String name, Object pool){
        ALIVE_REGISTRY.remove(name,pool);
    }
}

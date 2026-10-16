package com.guilu.utils;

import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class RegisDefaultNameUtil {
    private static final AtomicInteger poolRegisCount = new AtomicInteger(1);
    private static final String namePrefix = "dynamic_tp_";
    public static String createThreadPoolName(){
        return  namePrefix + poolRegisCount.getAndIncrement();
    }
    public static String getThreadPoolName(ThreadPoolExecutor threadPool){
        if(threadPool == null){
            return null;
        }
        try {
            Field threadFactory = ThreadPoolExecutor.class.getDeclaredField("threadFactory");
            threadFactory.setAccessible(true);
            ThreadFactory factory = (ThreadFactory) threadFactory.get(threadPool);
            return factory.getClass().getSimpleName();
        } catch (NoSuchFieldException | IllegalAccessException e) {
            log.error("getThreadPoolName—Error");
        }
        return createThreadPoolName();
    }
    public static String getThreadPoolName(Object threadPool){
        ThreadPoolExecutor threadPoolExecutor = null;
        if(threadPool instanceof  ThreadPoolExecutor){
            threadPoolExecutor = (ThreadPoolExecutor) threadPool;
        }
        return getThreadPoolName(threadPoolExecutor);
    }
}

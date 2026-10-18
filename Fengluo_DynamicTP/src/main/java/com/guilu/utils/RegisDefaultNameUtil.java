package com.guilu.utils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;

public class RegisDefaultNameUtil {
    private static final AtomicInteger SEQUENCE = new AtomicInteger();
    public static String createThreadPoolName() {
        return "dynamic_tp_" + SEQUENCE.incrementAndGet();
    }
    public static String getThreadPoolName(ThreadPoolExecutor pool) {
        return getThreadPoolName((Object) pool);
    }
    public static String getThreadPoolName(Object pool) {
        return pool instanceof ExecutorService ? createThreadPoolName() : null;
    }
}

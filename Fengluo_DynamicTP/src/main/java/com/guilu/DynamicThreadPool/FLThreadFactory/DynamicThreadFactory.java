package com.guilu.DynamicThreadPool.FLThreadFactory;


import org.springframework.lang.NonNull;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class DynamicThreadFactory implements ThreadFactory {

    private static volatile DynamicThreadFactory instance;
    private final ThreadGroup group;
    private final AtomicInteger threadNumber = new AtomicInteger(1);
    private final String namePrefix;

    private DynamicThreadFactory() {
        this.group = Thread.currentThread().getThreadGroup();
        this.namePrefix = "MapleDynamicPool-thread-";
    }

    public static DynamicThreadFactory getInstance() {
        if (instance == null) {
            synchronized (DynamicThreadFactory.class) {
                if (instance == null) {
                    instance = new DynamicThreadFactory();
                }
            }
        }
        return instance;
    }

    @Override
    public Thread newThread(@NonNull Runnable r) {
        Thread t = new Thread(group, r,
                namePrefix + threadNumber.getAndIncrement());
        t.setDaemon(false);
        t.setPriority(Thread.NORM_PRIORITY);
        return t;
    }
}

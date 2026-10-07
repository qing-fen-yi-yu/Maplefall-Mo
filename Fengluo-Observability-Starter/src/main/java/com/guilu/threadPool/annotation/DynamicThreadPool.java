package com.guilu.threadPool.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

//动态线程池扫描注解
@Target({ElementType.METHOD,ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface DynamicThreadPool {
    String name() default "";
    //是否扫描到后进行持久化存储
    boolean storage() default true;
}

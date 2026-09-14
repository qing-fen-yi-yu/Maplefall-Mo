package com.guilu.ThreadPoolOb.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

//todo 动态线程池注解，用于动态配置线程池的参数
@Target({ElementType.METHOD,ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface DynamicThreadPool {
    String name() default "";
}

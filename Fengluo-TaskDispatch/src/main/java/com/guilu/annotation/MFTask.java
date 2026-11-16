package com.guilu.annotation;

import com.guilu.enums.MisFireHandlingType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface MFTask {
    String name() default "";
    String scheduler() default "";
    //任务错过执行的策略-默认立即执行
    MisFireHandlingType mistype() default MisFireHandlingType.MISFIRE_INSTRUCTION_FIRE_NOW;
}

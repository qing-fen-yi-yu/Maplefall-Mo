package com.guilu.annotation;

import org.springframework.core.annotation.AliasFor;
import com.guilu.enums.MisFireHandlingType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface MFTask {
    @AliasFor("name")
    String value() default "";
    @AliasFor("value")
    String name() default "";
    // cron表达式
    String cron() default "";
    //执行器Bean
    String scheduler() default "";
    // Quartz misfire policy used by the cron trigger.
    MisFireHandlingType mistype() default MisFireHandlingType.MISFIRE_INSTRUCTION_FIRE_NOW;
    // Optional bean name of an org.quartz.Trigger.
    String trigger() default "";
}

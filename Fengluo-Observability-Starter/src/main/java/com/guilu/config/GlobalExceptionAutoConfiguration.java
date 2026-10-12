package com.guilu.config;

import com.guilu.exceptionHandler.GlobalExceptionHandler;
import com.guilu.exceptionHandler.chainAspect.GlobalExceptionAspect;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.aop.aspectj.annotation.AnnotationAwareAspectJAutoProxyCreator;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;

@Slf4j
@AutoConfiguration
@ConditionalOnProperty(
        prefix = "monitor.exception",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true
)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class GlobalExceptionAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public GlobalExceptionHandler globalExceptionHandler(MeterRegistry meterRegistry) {
        return new GlobalExceptionHandler(meterRegistry);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnClass(Aspect.class)
    public GlobalExceptionAspect globalExceptionAspect(GlobalExceptionHandler handler,
                                                       ConfigurableApplicationContext ctx) {
        boolean aopEnabled = ctx.getBeanNamesForType(
                AnnotationAwareAspectJAutoProxyCreator.class).length > 0;
        if (!aopEnabled) {
            log.warn("未检测到 AOP 后处理器，{} 全局异常链路将不会执行。"
                    + "请确认已引入 spring-boot-starter-aop", GlobalExceptionAspect.class.getName());
        } else {
            log.info("全局链路异常处理注册成功");
        }
        return new GlobalExceptionAspect(handler);
    }
}
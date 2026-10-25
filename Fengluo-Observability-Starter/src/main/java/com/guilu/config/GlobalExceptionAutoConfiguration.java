package com.guilu.config;

import com.guilu.exceptionHandler.GlobalExceptionHandler;
import com.guilu.exceptionHandler.chainAspect.GlobalExceptionAspect;
import io.micrometer.core.instrument.MeterRegistry;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.aop.aspectj.annotation.AnnotationAwareAspectJAutoProxyCreator;
import org.springframework.boot.actuate.autoconfigure.metrics.CompositeMeterRegistryAutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@AutoConfiguration(after = {CompositeMeterRegistryAutoConfiguration.class, AopAutoConfiguration.class})
@ConditionalOnClass({MeterRegistry.class, org.springframework.web.bind.annotation.RestControllerAdvice.class})
@ConditionalOnBean(MeterRegistry.class)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnProperty(prefix = "monitor.exception", name = "enabled", havingValue = "true", matchIfMissing = true)
public class GlobalExceptionAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean
    public GlobalExceptionHandler globalExceptionHandler(MeterRegistry registry) {
        return new GlobalExceptionHandler(registry);
    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(Aspect.class)
    @ConditionalOnBean(AnnotationAwareAspectJAutoProxyCreator.class)
    @ConditionalOnProperty(prefix = "spring.aop", name = "auto", havingValue = "true", matchIfMissing = true)
    static class ExceptionAspectConfiguration {
        @Bean
        @ConditionalOnMissingBean
        GlobalExceptionAspect globalExceptionAspect(GlobalExceptionHandler handler) {
            return new GlobalExceptionAspect(handler);
        }
    }
}

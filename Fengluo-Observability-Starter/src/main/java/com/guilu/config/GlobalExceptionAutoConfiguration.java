package com.guilu.config;

import com.guilu.exceptionHandler.GlobalExceptionHandler;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 业务异常处理器的自动配置。
 *
 * 这里的条件只负责「按需创建」，真正的防线在 GlobalExceptionHandler 类上：
 * 该类位于 com.guilu.exceptionHandler，会被各模块的组件扫描直接注册，
 * 自动配置的条件管不到它，因此两个条件在处理器类上都重复声明了一遍。
 */
@AutoConfiguration
@ConditionalOnProperty(
        prefix = "monitor.exception",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true
)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnClass(RestControllerAdvice.class)
public class GlobalExceptionAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public GlobalExceptionHandler globalExceptionHandler(MeterRegistry meterRegistry) {
        return new GlobalExceptionHandler(meterRegistry);
    }
}
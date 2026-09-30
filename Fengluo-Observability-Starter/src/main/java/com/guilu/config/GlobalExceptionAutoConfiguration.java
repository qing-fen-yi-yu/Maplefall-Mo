package com.guilu.config;

import com.guilu.exceptionHandler.GlobalExceptionHandler;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 业务异常处理器的自动配置，也就是 {@link GlobalExceptionHandler} 唯一的注册入口。
 */
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
    public GlobalExceptionHandler globalExceptionHandler(MeterRegistry meterRegistry) {
        return new GlobalExceptionHandler(meterRegistry);
    }
}

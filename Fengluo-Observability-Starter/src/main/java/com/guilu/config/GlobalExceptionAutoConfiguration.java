package com.guilu.config;

import com.fengluo.exception.GlobalExceptionHandler;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 业务异常处理器的自动配置，也就是 {@link GlobalExceptionHandler} 唯一的注册入口。
 *
 * <p>处理器类放在 {@code com.fengluo.observability.exception}，有意避开消费方的组件扫描范围
 * （各模块的 {@code @SpringBootApplication} 都在 {@code com.guilu}）。此前它位于
 * {@code com.guilu.exceptionHandler}，被组件扫描抢先注册成 Bean，导致本类的
 * {@code @ConditionalOnMissingBean} 工厂方法只会退让 —— 自动配置看起来「没执行」。
 *
 * <p>{@code @ConditionalOnWebApplication(SERVLET)} 是网关侧的关键防线：
 * 网关是 REACTIVE，本类整体不加载，处理器不会被注册，
 * {@code GatewayExceptionHandler} 才能正常接管鉴权异常并返回 Result 封装。
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
@ConditionalOnClass(RestControllerAdvice.class)
public class GlobalExceptionAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public GlobalExceptionHandler globalExceptionHandler(MeterRegistry meterRegistry) {
        return new GlobalExceptionHandler(meterRegistry);
    }

    /**
     * 本类被加载即打印，用于确认自动配置生效（配置类本身是 Bean，@PostConstruct 仅表示已加载）。
     */
    @PostConstruct
    public void init() {
        log.info("全局异常处理器注册成功......");
    }
}

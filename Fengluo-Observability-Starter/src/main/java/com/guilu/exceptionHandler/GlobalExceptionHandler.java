package com.guilu.exceptionHandler;

import com.guilu.exception.GlobalException;
import com.guilu.exception.RequestException.ForbiddenException;
import com.guilu.exception.RequestException.UnauthorizedException;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局业务异常处理器（仅 Servlet 环境）。
 *
 * 条件必须加在本类上，而不是只加在 GlobalExceptionAutoConfiguration 上：
 * 本类位于 com.guilu.exceptionHandler，落在各模块 @SpringBootApplication 的扫描路径内，
 * 网关会被「组件扫描」直接注册成 @RestControllerAdvice，与自动配置的条件无关。
 *
 * 为什么网关必须排除它：WebFlux 中 @ExceptionHandler 先于 ErrorWebExceptionHandler 执行，
 * 一旦注册就会遮住网关自己的 GatewayExceptionHandler，
 * 使鉴权失败返回 ProblemDetail 而不是项目约定的 Result 封装（前端读不到 code/message）。
 */
@RestControllerAdvice
@Order(Ordered.LOWEST_PRECEDENCE)  // 优先级最低，业务自己的处理器优先
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnProperty(
        prefix = "monitor.exception",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true
)
public class GlobalExceptionHandler {

    private final MeterRegistry meterRegistry;

    public GlobalExceptionHandler(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    @ExceptionHandler(GlobalException.class)
    public ResponseEntity<ProblemDetail> handleGlobal(GlobalException e) {
        String code = e.getCode() == null ? "unknown" : String.valueOf(e.getCode());
        meterRegistry.counter("business_exception_total",
                        "code", code,
                        "exception", String.valueOf(e.getMessage()))
                .increment();

        HttpStatus status = statusOf(e);
        return ResponseEntity.status(status)
                .body(ProblemDetail.forStatusAndDetail(status, e.getMessage()));
    }

    /**
     * 按异常类型决定 HTTP 状态。
     * 不能用 HttpStatus.valueOf(e.getCode()) —— e.getCode() 是业务码（如 40102），
     * 不是合法的 HTTP 状态码，会抛 IllegalArgumentException；code 为 null 时直接 NPE。
     */
    private HttpStatus statusOf(GlobalException e) {
        if (e instanceof UnauthorizedException) {
            return HttpStatus.UNAUTHORIZED;
        }
        if (e instanceof ForbiddenException) {
            return HttpStatus.FORBIDDEN;
        }
        return HttpStatus.BAD_REQUEST;
    }
}
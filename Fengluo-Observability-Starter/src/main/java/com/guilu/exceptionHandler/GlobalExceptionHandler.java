package com.guilu.exceptionHandler;

import com.guilu.exception.GlobalException;
import com.guilu.exception.RequestException.ForbiddenException;
import com.guilu.exception.RequestException.UnauthorizedException;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局业务异常处理器
 */
@Slf4j
@RestControllerAdvice
@Order(Ordered.LOWEST_PRECEDENCE)  // 优先级最低，业务自己的处理器优先
public class GlobalExceptionHandler implements com.guilu.exceptionHandler.ExceptionHandler {

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
    @ExceptionHandler(Exception.class)
    public  ResponseEntity<ProblemDetail> handleException(Exception e) {
        String message = e.getMessage();
        meterRegistry.counter("other_exception_total",
                "exception",message).increment();
        HttpStatus status = statusOf(e);
        return  ResponseEntity.status(status)
                .body(ProblemDetail.forStatusAndDetail(status, message));
    }

    /**
     * 按异常类型决定 HTTP 状态。
     * 不是合法的 HTTP 状态码，会抛 IllegalArgumentException；code 为 null 时直接 NPE。
     */
    private HttpStatus statusOf(Exception e) {
        if (e instanceof UnauthorizedException) {
            return HttpStatus.UNAUTHORIZED;
        }
        if (e instanceof ForbiddenException) {
            return HttpStatus.FORBIDDEN;
        }
        return HttpStatus.BAD_REQUEST;
    }
}

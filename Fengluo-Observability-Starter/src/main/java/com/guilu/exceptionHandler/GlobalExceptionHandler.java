package com.guilu.exceptionHandler;

import com.guilu.exception.GlobalException;
import com.guilu.exception.RequestException.ForbiddenException;
import com.guilu.exception.RequestException.UnauthorizedException;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局业务异常处理器
 */
@Slf4j
@RestControllerAdvice
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@Order(Ordered.LOWEST_PRECEDENCE)
public class GlobalExceptionHandler {

    private final MeterRegistry meterRegistry;

    public GlobalExceptionHandler(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    @ExceptionHandler(GlobalException.class)
    public ResponseEntity<ProblemDetail> handleGlobal(GlobalException e) {
        HttpStatusCode status = statusOf(e);
        return ResponseEntity.status(status)
                .body(ProblemDetail.forStatusAndDetail(status, e.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleException(Exception e) {
        String message = e.getMessage();
        registryHandler(e);
        HttpStatusCode status = statusOf(e);
        return ResponseEntity.status(status)
                .body(ProblemDetail.forStatusAndDetail(status, message));
    }

    private HttpStatusCode statusOf(Exception e) {
        if (e instanceof ErrorResponse error) {
            return error.getStatusCode();
        }
        if (e instanceof UnauthorizedException) {
            return HttpStatus.UNAUTHORIZED;
        }
        if (e instanceof ForbiddenException) {
            return HttpStatus.FORBIDDEN;
        }
        return e instanceof GlobalException ? HttpStatus.BAD_REQUEST : HttpStatus.INTERNAL_SERVER_ERROR;
    }

    public void chainHandler(Throwable e) {
        registryHandler(e);
    }
    private void registryHandler(Throwable e){
        /**
         * 只捕获三级异常，自定义global->RunTime->Exception
         */
        String message = e.getMessage();
        if(e instanceof GlobalException globalException){
            String code = globalException.getCode() == null ? "unknown" : String.valueOf(globalException.getCode());
            meterRegistry.counter("business_exception_total",
                            "code", code,
                            "exception", String.valueOf(e.getMessage()))
                    .increment();
        } else if(e instanceof RuntimeException){
            meterRegistry.counter("runtime_exception_total",
                    "exception", message).increment();
        } else if(e instanceof  Exception){
            meterRegistry.counter("other_exception_total","exception",message).increment();
        }
        meterRegistry.counter("exception_total").increment();
    }
}
package com.guilu.exceptionHandler;

import com.guilu.exception.GlobalException;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Order(Ordered.LOWEST_PRECEDENCE)  // 优先级最低，业务自己的处理器优先
public class GlobalExceptionHandler {

    private final MeterRegistry meterRegistry;

    public GlobalExceptionHandler(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    @ExceptionHandler(GlobalException.class)
    public ResponseEntity<ErrorResponse> handleGlobal(GlobalException e) {
        String code = e.getCode() == null ? "unknown" : String.valueOf(e.getCode());
        meterRegistry.counter("business_exception_total",
                        "code", code,
                        "exception", e.getMessage())
                .increment();

        return ResponseEntity.badRequest()
                .body(new ErrorResponse() {
                    @Override
                    public HttpStatusCode getStatusCode() {
                        return HttpStatus.valueOf(e.getCode());
                    }

                    @Override
                    public ProblemDetail getBody() {
                        return ProblemDetail.forStatusAndDetail(
                                HttpStatus.valueOf(e.getCode()),
                                e.getMessage());
                    }
                });
    }
}
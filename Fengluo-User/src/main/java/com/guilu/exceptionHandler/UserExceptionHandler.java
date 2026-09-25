package com.guilu.exceptionHandler;

import com.guilu.domain.Result;
import com.guilu.exception.GlobalException;
import com.guilu.exception.RequestException.ForbiddenException;
import com.guilu.exception.RequestException.UnauthorizedException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import static com.guilu.constants.ResultInfo.Code.FAILED;

/**
 * 业务异常到 HTTP 状态的映射。
 * <p>
 * Spring Cloud Gateway 有自己的 {@code GatewayExceptionHandler}，但业务模块此前没有任何
 * {@code @RestControllerAdvice}，认证失败会退化成 500。这里按异常类型显式映射状态码，
 * 不用 {@code e.getCode()} 反查 HttpStatus —— 业务码（如 40102）不是合法的 HTTP 状态码，
 * 那样做会在异常处理器内部再抛异常。
 */
@Slf4j
@RestControllerAdvice
public class UserExceptionHandler {

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<Result<Void>> handleUnauthorized(UnauthorizedException e) {
        log.warn("认证失败: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Result.error(codeOf(e, HttpStatus.UNAUTHORIZED.value()), e.getMessage()));
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<Result<Void>> handleForbidden(ForbiddenException e) {
        log.warn("权限不足: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Result.error(codeOf(e, HttpStatus.FORBIDDEN.value()), e.getMessage()));
    }

    @ExceptionHandler(GlobalException.class)
    public ResponseEntity<Result<Void>> handleGlobal(GlobalException e) {
        log.warn("业务异常: {}", e.getMessage());
        return ResponseEntity.badRequest()
                .body(Result.error(codeOf(e, FAILED), e.getMessage()));
    }

    private int codeOf(GlobalException e, int fallback) {
        return e.getCode() == null ? fallback : e.getCode();
    }
}

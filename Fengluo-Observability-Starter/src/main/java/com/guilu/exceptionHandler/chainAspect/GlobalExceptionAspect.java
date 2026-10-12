package com.guilu.exceptionHandler.chainAspect;

import com.guilu.exceptionHandler.GlobalExceptionHandler;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;

@Aspect
public record GlobalExceptionAspect(GlobalExceptionHandler handler) {
    private static final Log log = LogFactory.getLog(GlobalExceptionAspect.class);

    @AfterThrowing(
            pointcut = "@within(com.guilu.exceptionHandler.annotations.GlobalChainHandler)",
            throwing = "ex"
    )
    public void chainAspectHandler(JoinPoint joinPoint, Throwable ex) throws Throwable {
        handler.chainHandler(ex);
        log.info("捕获异常{}",ex);
    }
}

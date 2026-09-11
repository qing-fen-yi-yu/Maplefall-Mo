package com.guilu.ExceptionHandler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.URI;

/**
 * todo网关异常处理器
 */
@Slf4j
@Component
public class GatewayExceptionHandler implements ErrorWebExceptionHandler, Ordered {

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        // 1.获取响应
        ServerHttpResponse response = exchange.getResponse();
        // 2.判断是否已处理
        if (response.isCommitted()) {
            // 如果已经提交，直接结束，避免重复处理
            return Mono.error(ex);
        }
        return null;
    }

    private void writeLog(ServerWebExchange exchange, Throwable ex) {
        ServerHttpRequest request = exchange.getRequest();
        URI uri = request.getURI();
        String host = uri.getHost();
        int port = uri.getPort();
        log.error("网关路由异常-host:{} ,port:{}，uri:{},  errormessage:",
                host, port, request.getPath(), ex);
    }

    @Override
    public int getOrder() {
        return HIGHEST_PRECEDENCE;
    }
}
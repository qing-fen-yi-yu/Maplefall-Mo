package com.guilu.filter;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;

import static com.guilu.constants.Constant.*;

@Slf4j
@Component
public class RequestIdRelayFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // 1. 优先复用上游传入的 requestId
        String requestId = exchange.getRequest().getHeaders().getFirst(REQUEST_ID_HEADER);
        if (requestId == null || requestId.isEmpty()) {
            requestId = UUID.randomUUID().toString();
        }

        String path = exchange.getRequest().getPath().toString();
        final String finalRequestId = requestId;

        // 2. 响应里也带上 requestId，便于前端排查
        exchange.getResponse().getHeaders().add(REQUEST_ID_HEADER, finalRequestId);

        // 3. 改写请求头
        ServerWebExchange mutated = exchange.mutate().request(b -> {
            b.header(REQUEST_ID_HEADER, finalRequestId);
            if (!path.startsWith(NOTIFY_PATH_PREFIX)) {
                b.header(REQUEST_FROM_HEADER, GATEWAY_ORIGIN_NAME);
            }
        }).build();

//        if (log.isDebugEnabled()) {
//            log.debug("gateway relay requestId = {}, path = {}", finalRequestId, path);
//        }

        // 4. 写入 Reactor Context，供下游 filter / 日志框架使用
        return chain.filter(mutated)
                .contextWrite(ctx -> ctx.put(REQUEST_ID_HEADER, finalRequestId));
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
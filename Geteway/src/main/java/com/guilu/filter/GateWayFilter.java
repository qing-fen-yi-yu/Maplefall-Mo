package com.guilu.filter;

import com.guilu.config.AuthProperties;
import com.guilu.exception.RequestException.ForbiddenException;
import com.guilu.util.PathUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import static com.guilu.constants.AuthErrorInfo.Msg.FORBIDDEN;

@Slf4j
@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties({AuthProperties.class})
public class GateWayFilter implements GlobalFilter, Ordered {
    private final AuthProperties authProperties;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getPath().toString();
        log.info("当前请求路径+{}",path);
        // 1.命中白名单路径，直接放行
        if (PathUtil.isPath(path, pathMatcher, authProperties.resolveGatewayExclude())) {
            return chain.filter(exchange);
        }
        // 2.命中拦截路径，禁止访问
        if (PathUtil.isPath(path, pathMatcher, authProperties.resolveGatewayInclude())) {
            throw new ForbiddenException(FORBIDDEN);
        }
        // 3.其余路径放行
        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return HIGHEST_PRECEDENCE+100;
    }
}

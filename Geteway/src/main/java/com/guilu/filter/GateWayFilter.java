package com.guilu.filter;

import com.guilu.config.AuthProperties;
import com.guilu.util.PathUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties({AuthProperties.class})
public class GateWayFilter implements GlobalFilter, Ordered {
    private final AuthProperties authProperties;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        //如果是非白名单路径，直接放行
        if (PathUtil.isPath(
                exchange.getRequest().getPath().toString(), pathMatcher
                , authProperties.getGatewayExcludePaths())){
            // 直接放行
            return chain.filter(exchange);
        }
        //判断路径是否存在被拦截路径中
        if(PathUtil.isPath(
                exchange.getRequest().getPath().toString(), pathMatcher
                , authProperties.getGatewayIncludePaths())){
            // 直接放行
            return null;
        }
        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return HIGHEST_PRECEDENCE+100;
    }
}

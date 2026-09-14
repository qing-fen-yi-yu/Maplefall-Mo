package com.guilu.filter;

import com.guilu.config.ResourceAuthProperties;
import com.guilu.domain.Result;
import com.guilu.domain.dto.LoginUserDTO;
import com.guilu.util.AuthUtil;
import com.guilu.util.PathUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

import static com.guilu.constants.JwtConstants.AUTHORIZATION_HEADER;
import static com.guilu.constants.JwtConstants.USER_HEADER;

/***
 *
 */
@Configuration
@EnableConfigurationProperties(ResourceAuthProperties.class)
@RequiredArgsConstructor
public class AccountAuthFilter implements GlobalFilter, Ordered {
    private final ResourceAuthProperties resourceAuthProperties;
    private final PathMatcher pathMatcher = new AntPathMatcher();
    private final AuthUtil authUtil;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // 1.获取请求request信息
        ServerHttpRequest request = exchange.getRequest();
        String method = request.getMethod().toString();
        String path = request.getPath().toString();
        String antPath = method + ":" + path;
//         2.判断是否是无需登录的路径
        if(PathUtil.isPath(
                antPath, pathMatcher
                , resourceAuthProperties.getExcludeLoginPaths())){
            // 直接放行
            return chain.filter(exchange);
        }
        // 3.尝试获取用户信息
        List<String> authHeaders = exchange.getRequest().getHeaders().get(AUTHORIZATION_HEADER);
        String token = authHeaders == null ? "" : authHeaders.get(0);
        Result<LoginUserDTO> r = authUtil.parseToken(token);

        // 4.如果用户是登录状态，尝试更新请求头，传递用户信息
        if(r.isLoginStatus()){
            exchange.mutate()
                    .request(builder -> builder.header(USER_HEADER, r.getData().getUserId().toString()))
                    .build();
        }
        // 5.校验权限
        authUtil.checkAuth(antPath, r);
        // 6.放行
        return chain.filter(exchange);
    }
    @Override
    public int getOrder() {
        return 1000;
    }
}

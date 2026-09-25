package com.guilu.filter;

import com.guilu.config.ResourceAuthProperties;
import com.guilu.domain.Result;
import com.guilu.domain.dto.LoginUserDTO;
import com.guilu.resolver.AuthMetadataResolver;
import com.guilu.util.AuthUtil;
import com.guilu.util.PathUtil;
import jakarta.annotation.PostConstruct;
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

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

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
    private final AuthMetadataResolver authMetadataResolver;

    /** 网关本地配置的排除路径（归一化后） */
    private Set<String> localExcludePaths = Set.of();

    @PostConstruct
    public void init() {
        localExcludePaths = PathUtil.normalize(resourceAuthProperties.getExcludeLoginPaths());
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // 未开启登录拦截则直接放行
        if (!Boolean.TRUE.equals(resourceAuthProperties.getEnable())) {
            return chain.filter(exchange);
        }
        // 1.获取请求request信息
        ServerHttpRequest request = exchange.getRequest();
        String method = request.getMethod() == null ? "" : request.getMethod().toString();
        String path = request.getPath().toString();
        String antPath = method + ":" + path;
        // 2.判断是否是无需登录的路径（网关本地配置 + 各业务模块上报的Nacos元数据）
        if (PathUtil.isPath(antPath, pathMatcher, excludePaths())) {
            // 直接放行
            return chain.filter(exchange);
        }
        // 3.尝试获取用户信息
        List<String> authHeaders = exchange.getRequest().getHeaders().get(AUTHORIZATION_HEADER);
        String token = authHeaders == null ? "" : authHeaders.get(0);
        Result<LoginUserDTO> r = authUtil.parseToken(token);

        // 4.如果用户是登录状态，更新请求头传递用户信息（重新赋值 exchange）
        if (r.isLoginStatus()) {
            exchange = exchange.mutate()
                    .request(builder ->
                            builder.header(USER_HEADER, r.getData().getUserId().toString()))
                    .build();
        }
        // 5.校验权限
        authUtil.checkAuth(antPath, r);
        // 6.放行
        return chain.filter(exchange);
    }

    private Set<String> excludePaths() {
        Set<String> excludes = new LinkedHashSet<>(localExcludePaths);
        excludes.addAll(authMetadataResolver.getExcludePaths());
        return excludes;
    }

    /**
     * 必须排在路由过滤器 StripPrefix 之前。
     * Gateway 把路由级过滤器包成 OrderedGatewayFilter，顺序为 index+1，
     * 即 StripPrefix=1；若本过滤器顺序大于 1，网关已把 /user 前缀剥掉，
     * 而权限表达式（sys_menu.permission）与各业务模块上报的排除路径都带 /user 前缀，
     * 匹配全部落空 -> findMatchPath 返回 null -> 静默放行，鉴权形同虚设。
     * 取 HIGHEST_PRECEDENCE+100 之后、StripPrefix 之前，与 GateWayFilter 的取值保持一致。
     */
    @Override
    public int getOrder() {
        return HIGHEST_PRECEDENCE + 200;
    }
}

package com.guilu.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.LinkedHashSet;
import java.util.Set;

@Data
@ConfigurationProperties(prefix = "fl.auth.gateway")
public class AuthProperties {

    private static final Set<String> DEFAULT_GATEWAY = Set.of(
            "/error/**", "/jwks", "/*/login", "/**/admin/login", "/accounts/refresh"
    );

    /** 追加的排除路径（在默认基础上叠加） */
    private Set<String> gatewayExcludePaths = new LinkedHashSet<>();

    /** 需要拦截的路径（为空表示不启用 include 模式） */
    private Set<String> gatewayIncludePaths = new LinkedHashSet<>();

    /** 是否禁用内置默认排除项*/
    private boolean disableDefaultExcludes = true;

    public Set<String> resolveGatewayExclude() {
        if (disableDefaultExcludes) {
            return new LinkedHashSet<>(gatewayExcludePaths);
        }
        Set<String> r = new LinkedHashSet<>(DEFAULT_GATEWAY);
        r.addAll(gatewayExcludePaths);
        return r;
    }

    public Set<String> resolveGatewayInclude() {
        return new LinkedHashSet<>(gatewayIncludePaths);
    }
}

package com.guilu.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.LinkedHashSet;
import java.util.Set;

@Data
@Configuration
@ConfigurationProperties("fl.auth")
public class AuthProperties {

    private Set<String> gatewayExcludePaths = new LinkedHashSet<>();

    private static final Set<String> DEFAULT_GATEWAY = Set.of(
            "/error/**", "/jwks", "/**/login", "/**/admin/login", "/accounts/refresh"
    );
    public Set<String> resolveGatewayExclude() {
        Set<String> r = new LinkedHashSet<>(DEFAULT_GATEWAY);
        if (gatewayExcludePaths != null) r.addAll(gatewayExcludePaths);
        return r;
    }

}

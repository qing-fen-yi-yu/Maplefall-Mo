package com.guilu.config;

import lombok.Data;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.HashSet;
import java.util.Set;

@Data
@Configuration
@ConfigurationProperties("fl.auth.exclude")
public class AuthProperties implements InitializingBean {
    private Set<String> excludePath;
    @Override
    public void afterPropertiesSet() throws Exception {
        if (excludePath == null) {
            excludePath = new HashSet<>();
        }
        // 添加默认不拦截的路径
        excludePath.add("/error/**");
        excludePath.add("/jwks");
        excludePath.add("*/login");
        excludePath.add("*/admin/login");
        excludePath.add("/accounts/refresh");
    }
}

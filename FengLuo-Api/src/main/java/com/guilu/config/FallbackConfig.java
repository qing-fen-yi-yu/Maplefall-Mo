package com.guilu.config;

import com.guilu.client.Auth.fallback.RoleClientFallback;
import com.guilu.client.Auth.fallback.UserClientFallback;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FallbackConfig {
    public UserClientFallback userClientFallback(){
        return new UserClientFallback();
    }
    public RoleClientFallback roleClientFallback(){
        return new RoleClientFallback();
    }
}

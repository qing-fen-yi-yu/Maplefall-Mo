package com.guilu.config;

import com.guilu.interceptor.UserContextInterceptor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 业务模块侧自动配置：注册将 user-info 请求头写入 {@link com.guilu.utils.UserContext} 的拦截器。
 * 仅在 Servlet（WebMvc）环境下生效，网关（WebFlux）不受影响。
 */
@AutoConfiguration
@ConditionalOnClass(WebMvcConfigurer.class)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class ResourceInterceptorConfiguration implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new UserContextInterceptor()).addPathPatterns("/**");
    }
}

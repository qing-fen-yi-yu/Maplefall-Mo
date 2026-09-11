package com.guilu.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableConfigurationProperties(ResourceAuthProperties.class)
public class ResourceInterceptorConfiguration implements WebMvcConfigurer {
    // 注入属性类
    private final ResourceAuthProperties authProperties;

    @Autowired
    public ResourceInterceptorConfiguration(ResourceAuthProperties resourceAuthProperties) {
        this.authProperties = resourceAuthProperties;
    }
    //todo
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
    }

}
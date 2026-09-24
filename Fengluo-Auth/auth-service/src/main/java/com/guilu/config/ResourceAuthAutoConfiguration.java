package com.guilu.config;

import com.alibaba.cloud.nacos.NacosDiscoveryProperties;
import com.guilu.metadata.AuthMetadataRegistrar;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * 业务模块侧自动配置：注册 {@link ResourceAuthProperties}，
 * 并在存在 Nacos 服务发现时，把登录拦截路径上报为实例元数据。
 */
@AutoConfiguration
@ConditionalOnClass(NacosDiscoveryProperties.class)
@EnableConfigurationProperties(ResourceAuthProperties.class)
public class ResourceAuthAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public static AuthMetadataRegistrar authMetadataRegistrar(ResourceAuthProperties authProperties) {
        return new AuthMetadataRegistrar(authProperties);
    }
}

package com.guilu.config;

import com.guilu.access.PrivilegeAccessor;
import com.guilu.service.PrivilegeService;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * 权限缓存发布自动配置：仅当业务侧提供了 {@link PrivilegeAccessor} 实现时注册，
 * 避免在没有权限数据源的模块（如网关）中误发布空缓存。
 */
@AutoConfiguration
@ConditionalOnClass(StringRedisTemplate.class)
public class PrivilegeAutoConfiguration {

    @Bean
    @ConditionalOnBean(PrivilegeAccessor.class)
    @ConditionalOnMissingBean
    public PrivilegeService privilegeService(PrivilegeAccessor privilegeAccessor,
                                             StringRedisTemplate stringRedisTemplate) {
        return new PrivilegeService(privilegeAccessor, stringRedisTemplate);
    }
}

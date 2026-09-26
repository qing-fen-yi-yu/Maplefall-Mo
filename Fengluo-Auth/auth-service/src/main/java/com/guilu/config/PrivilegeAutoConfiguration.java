package com.guilu.config;

import com.guilu.access.PrivilegeAccessor;
import com.guilu.service.PrivilegeService;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.StringRedisTemplate;

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

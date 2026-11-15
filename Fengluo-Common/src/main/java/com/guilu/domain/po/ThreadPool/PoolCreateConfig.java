package com.guilu.domain.po.ThreadPool;

import com.guilu.enums.TPType;
import com.guilu.utils.BeanUtils;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Data
public class PoolCreateConfig extends BaseThreadPoolConfig {
    private String service;
    private Integer port;
    private String host;
    private TPType executorType;
    public static PoolCreateConfig of(BaseThreadPoolConfig threadPoolConfig, TPType type,String service,String instanceConfig) {
        PoolCreateConfig poolCreateConfig = BeanUtils.copyBean(threadPoolConfig, PoolCreateConfig.class);
        poolCreateConfig.setService(service);
        if (instanceConfig != null && !instanceConfig.isBlank()) {
            int separator = instanceConfig.lastIndexOf(':');
            if (separator <= 0 || separator == instanceConfig.length() - 1) {
                throw new IllegalArgumentException("服务实例配置格式应为 host:port: " + instanceConfig);
            }
            poolCreateConfig.setExecutorType(type);
            poolCreateConfig.setHost(instanceConfig.substring(0, separator));
            poolCreateConfig.setPort(Integer.parseInt(instanceConfig.substring(separator + 1)));
        }
        return poolCreateConfig;
    }
}

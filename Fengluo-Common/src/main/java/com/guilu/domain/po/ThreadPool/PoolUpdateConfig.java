package com.guilu.domain.po.ThreadPool;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.beans.BeanUtils;

/** 线程池更新参数，以及更新目标的服务和实例信息。 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class PoolUpdateConfig extends BaseThreadPoolConfig {
    private String service;
    private Integer port;
    private String host;

    public static PoolUpdateConfig ofPoolUpdateConfig(BaseThreadPoolConfig config, String service,
                                                     String host, Integer port) {
        PoolUpdateConfig poolUpdateConfig = of(config, service);
        poolUpdateConfig.setHost(host);
        poolUpdateConfig.setPort(port);
        return poolUpdateConfig;
    }

    public static PoolUpdateConfig ofPoolUpdateConfig(BaseThreadPoolConfig config, String service,
                                                     String instanceConfig) {
        PoolUpdateConfig poolUpdateConfig = of(config, service);
        // 实例为空时表示服务级配置。
        if (instanceConfig == null || instanceConfig.isBlank()) {
            return poolUpdateConfig;
        }
        int separator = instanceConfig.lastIndexOf(':');
        if (separator <= 0 || separator == instanceConfig.length() - 1) {
            throw new IllegalArgumentException("服务实例配置格式应为 host:port: " + instanceConfig);
        }
        poolUpdateConfig.setHost(instanceConfig.substring(0, separator));
        poolUpdateConfig.setPort(Integer.parseInt(instanceConfig.substring(separator + 1)));
        return poolUpdateConfig;
    }

    private static PoolUpdateConfig of(BaseThreadPoolConfig config, String service) {
        PoolUpdateConfig poolUpdateConfig = new PoolUpdateConfig();
        BeanUtils.copyProperties(config, poolUpdateConfig, BaseThreadPoolConfig.class);
        poolUpdateConfig.setService(service);
        return poolUpdateConfig;
    }
}

package com.guilu.metadata;

import com.alibaba.cloud.nacos.NacosDiscoveryProperties;
import com.guilu.config.ResourceAuthProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.BeanPostProcessor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.guilu.constants.AuthMetadataConstants.EXCLUDE_PATHS_KEY;
import static com.guilu.constants.AuthMetadataConstants.INCLUDE_PATHS_KEY;
import static com.guilu.constants.AuthMetadataConstants.PATH_DELIMITER;

/**
 * 业务模块侧：把本地 fl.auth.resource 中配置的登录拦截路径，
 * 以实例元数据的形式上报到 Nacos，供网关聚合并用于路径放行判断。
 * 保证早于服务注册（NacosRegistration 由该 bean 派生），使上报一定生效。
 */
@Slf4j
public class AuthMetadataRegistrar implements BeanPostProcessor {

    private final ResourceAuthProperties authProperties;

    public AuthMetadataRegistrar(ResourceAuthProperties authProperties) {
        this.authProperties = authProperties;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        if (bean instanceof NacosDiscoveryProperties nacosProperties) {
            publish(nacosProperties);
        }
        return bean;
    }

    private void publish(NacosDiscoveryProperties nacosProperties) {
        Map<String, String> metadata = nacosProperties.getMetadata();
        if (metadata == null) {
            metadata = new HashMap<>();
            nacosProperties.setMetadata(metadata);
        }
        boolean published = false;
        published |= putIfPresent(metadata, EXCLUDE_PATHS_KEY, authProperties.getExcludeLoginPaths());
        published |= putIfPresent(metadata, INCLUDE_PATHS_KEY, authProperties.getIncludeLoginPaths());
        if (published) {
            log.debug("上报网关鉴权路径元数据: {}", metadata);
        }
    }

    private boolean putIfPresent(Map<String, String> metadata, String key, List<String> paths) {
        String joined = join(paths);
        if (joined.isEmpty()) {
            return false;
        }
        metadata.put(key, joined);
        return true;
    }

    private String join(List<String> paths) {
        if (paths == null || paths.isEmpty()) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        for (String path : paths) {
            if (path == null || path.isBlank()) {
                continue;
            }
            if (builder.length() > 0) {
                builder.append(PATH_DELIMITER);
            }
            builder.append(path.trim());
        }
        return builder.toString();
    }
}

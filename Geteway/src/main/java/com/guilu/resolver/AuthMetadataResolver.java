package com.guilu.resolver;

import com.guilu.util.PathUtil;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.discovery.event.HeartbeatEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static com.guilu.constants.AuthMetadataConstants.EXCLUDE_PATHS_KEY;
import static com.guilu.constants.AuthMetadataConstants.INCLUDE_PATHS_KEY;
import static com.guilu.constants.AuthMetadataConstants.PATH_DELIMITER;

/**
 * 网关侧：聚合各业务模块通过 Nacos 实例元数据上报的登录拦截路径。
 * <p>
 * 业务模块的 exclude/include-login-paths 由其 AuthMetadataRegistrar 发布到实例元数据，
 * 这里在启动时以及服务变更（{@link HeartbeatEvent}）时汇总，供 AccountAuthFilter 使用。
 */
@Slf4j
@Component
public class AuthMetadataResolver {

    private final DiscoveryClient discoveryClient;

    private volatile Set<String> excludePaths = Set.of();
    private volatile Set<String> includePaths = Set.of();

    public AuthMetadataResolver(DiscoveryClient discoveryClient) {
        this.discoveryClient = discoveryClient;
    }

    @PostConstruct
    public void init() {
        refresh();
    }

    @EventListener(HeartbeatEvent.class)
    public void onHeartbeat(HeartbeatEvent event) {
        refresh();
    }

    public synchronized void refresh() {
        Set<String> excludes = new HashSet<>();
        Set<String> includes = new HashSet<>();
        try {
            for (String service : discoveryClient.getServices()) {
                for (ServiceInstance instance : discoveryClient.getInstances(service)) {
                    Map<String, String> metadata = instance.getMetadata();
                    if (metadata == null) {
                        continue;
                    }
                    collect(metadata.get(EXCLUDE_PATHS_KEY), excludes);
                    collect(metadata.get(INCLUDE_PATHS_KEY), includes);
                }
            }
        } catch (Exception e) {
            // 服务发现不可用时保留上一次的聚合结果，避免放行判断失效
            log.error("聚合网关鉴权路径元数据失败", e);
            return;
        }
        this.excludePaths = PathUtil.normalize(excludes);
        this.includePaths = PathUtil.normalize(includes);
        log.debug("网关鉴权路径元数据刷新完成, exclude={}, include={}", excludePaths, includePaths);
    }

    private void collect(String value, Set<String> target) {
        if (value == null || value.isBlank()) {
            return;
        }
        for (String path : value.split(PATH_DELIMITER)) {
            if (!path.isBlank()) {
                target.add(path.trim());
            }
        }
    }

    public Set<String> getExcludePaths() {
        return excludePaths;
    }

    public Set<String> getIncludePaths() {
        return includePaths;
    }
}

package com.guilu.DynamicTPConfig;

import com.alibaba.cloud.nacos.NacosDiscoveryProperties;
import com.alibaba.cloud.nacos.registry.NacosRegistration;
import com.alibaba.cloud.nacos.registry.NacosServiceRegistry;
import com.guilu.Manager.reporter.PoolReporter;
import com.guilu.constants.DynamicTPMetaKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class TpMetaDataReporter {

    private final PoolReporter poolReporter;
    private final NacosDiscoveryProperties properties;
    private final NacosServiceRegistry serviceRegistry;
    private final NacosRegistration registration;

    public TpMetaDataReporter(PoolReporter poolReporter,
                              NacosDiscoveryProperties properties,
                              NacosServiceRegistry serviceRegistry,
                              NacosRegistration registration) {
        this.poolReporter = poolReporter;
        this.properties = properties;
        this.serviceRegistry = serviceRegistry;
        this.registration = registration;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void reportOnce() {
        try {
            String json = poolReporter.snapshotAsJson();
            if (json == null || json.isBlank()) {
                return;
            }
            Map<String, String> metadata = new HashMap<>(properties.getMetadata());
            metadata.put(DynamicTPMetaKey.DYNAMIC_TP_SNAPSHOT_KEY, json);
            properties.setMetadata(metadata);
            serviceRegistry.register(registration);
        } catch (Exception e) {
            log.error("上报线程池快照到 Nacos 元数据失败", e);
        }
    }
}
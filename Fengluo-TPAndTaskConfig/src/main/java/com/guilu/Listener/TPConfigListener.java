package com.guilu.Listener;

import cn.hutool.core.lang.TypeReference;
import com.guilu.domain.DynamicPoolSnapshot;
import com.guilu.instance.AbstractInstanceManager;
import com.guilu.instance.InstanceManager;
import com.guilu.utils.JsonUtils;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import com.guilu.constants.DynamicTPMetaKey;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
@Component
public class TPConfigListener extends AbstractInstanceManager {

    private static final long ALIVE_TIME = 60000L;
    private final InstanceManager instanceManager;
    @Getter
    private final Map<String, Set<DynamicPoolSnapshot>> dynamicPoolSnapshotMap = new ConcurrentHashMap<>();

    public TPConfigListener(InstanceManager instanceManager) {
        this.instanceManager = instanceManager;
        instanceManager.register(this);
    }

    @EventListener(ApplicationReadyEvent.class)
    @Scheduled(cron = "*/30 * * * * *")
    public void refreshInstanceAndSnapshot() {
        refreshData();
    }
    private void refreshData() {
        for (Map.Entry<String, ServiceInstance> entry : instanceManager.getInstanceMap().entrySet()) {
            String instanceKey = entry.getKey();
            ServiceInstance instance = entry.getValue();
            try {
                Map<String, String> metadata = instance.getMetadata();
                if (metadata == null) {
                    continue;
                }
                String json = metadata.get(DynamicTPMetaKey.DYNAMIC_TP_SNAPSHOT_KEY);
                if (json == null || json.isEmpty()) {
                    continue;
                }
                Map<String, DynamicPoolSnapshot> tpSnapshot = JsonUtils.toBean(
                        json, new TypeReference<Map<String, DynamicPoolSnapshot>>() {});
                if (tpSnapshot == null || tpSnapshot.isEmpty()) {
//                    log.warn("实例 {} 快照解析为空", instanceKey);
                    continue;
                }
                Set<DynamicPoolSnapshot> set = Collections.newSetFromMap(new ConcurrentHashMap<>());
                set.addAll(tpSnapshot.values());
                for (DynamicPoolSnapshot dynamicPoolSnapshot : set) {
                    dynamicPoolSnapshot.setService(instanceKey);
                }
                dynamicPoolSnapshotMap.put(instanceKey, set);
            } catch (Exception e) {
                log.error("拉取实例 {} 线程池数据失败", instanceKey, e);
            }
        }
    }

    public Collection<DynamicPoolSnapshot> getDynamicPoolSnapshotByService(String service) {
        String prefix = service + "@";
        return dynamicPoolSnapshotMap.entrySet().stream()
                .filter(e -> e.getKey().startsWith(prefix))
                .flatMap(e -> e.getValue().stream())
                .collect(Collectors.toSet());
    }

    public Collection<DynamicPoolSnapshot> getDynamicPoolSnapshotByService(String service, String host, Integer port) {
        Set<DynamicPoolSnapshot> set = dynamicPoolSnapshotMap.get(instanceManager.getService(service, host, port));
        return set == null ? Collections.emptyList() :
                new HashSet<>(set);
    }

    @Override
    public void clear(Collection<String> removeInstance) {
        removeInstance.forEach(dynamicPoolSnapshotMap::remove);
    }
}

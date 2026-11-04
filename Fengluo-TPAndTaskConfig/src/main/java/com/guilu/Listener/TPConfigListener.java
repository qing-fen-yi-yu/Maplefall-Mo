package com.guilu.Listener;

import cn.hutool.core.lang.TypeReference;
import com.guilu.domain.DynamicPoolSnapshot;
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
public class TPConfigListener {

    private static final long ALIVE_TIME = 60000L;

    @Getter
    private final Map<String, Set<DynamicPoolSnapshot>> dynamicPoolSnapshotMap = new ConcurrentHashMap<>();
    private final DiscoveryClient discoveryClient;
    private final Map<String, ServiceInstance> instanceMap = new ConcurrentHashMap<>();
    private final Map<String, Long> instanceLastTime = new ConcurrentHashMap<>();

    public TPConfigListener(DiscoveryClient discoveryClient) {
        this.discoveryClient = discoveryClient;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        refreshInstanceAndSnapshot();
    }

    @Scheduled(cron = "*/30 * * * * *")
    public void refreshInstanceAndSnapshot() {
        initInstance();
        refreshData();
        cleanExpired();
    }

    private void initInstance() {
        if (discoveryClient == null) {
            log.error("discoveryClient is null");
            return;
        }
        List<String> services = discoveryClient.getServices();
        if (services == null || services.isEmpty()) {
            log.warn("注册中心未返回服务列表，跳过本轮扫描");
            return;
        }
        long now = System.currentTimeMillis();
        for (String service : services) {
            List<ServiceInstance> instances = discoveryClient.getInstances(service);
            if (instances == null) {
                continue;
            }
            for (ServiceInstance instance : instances) {
                String instanceKey = getService(service, instance);
                if(instance.getMetadata().containsKey(DynamicTPMetaKey.DYNAMIC_TP_SNAPSHOT_KEY)){
                    instanceMap.put(instanceKey, instance);
                    if(discoveryClient.getInstances(instanceKey.substring(0,instanceKey.indexOf("@"))) != null){
                        instanceLastTime.put(instanceKey, now);
                    }
                }
            }
        }
    }

    private void refreshData() {
        for (Map.Entry<String, ServiceInstance> entry : instanceMap.entrySet()) {
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
                    log.warn("实例 {} 快照解析为空", instanceKey);
                    continue;
                }
                Set<DynamicPoolSnapshot> set = Collections.newSetFromMap(new ConcurrentHashMap<>());
                set.addAll(tpSnapshot.values());
                dynamicPoolSnapshotMap.put(instanceKey, set);
            } catch (Exception e) {
                log.error("拉取实例 {} 线程池数据失败", instanceKey, e);
            }
        }
        log.info("刷新线程池数据完成, 共 {} 个实例", instanceMap.size());
        log.info("当前线程池数据: {}", dynamicPoolSnapshotMap);
    }

    private void cleanExpired() {
        long now = System.currentTimeMillis();
        for (Map.Entry<String, Long> entry : instanceLastTime.entrySet()) {
            if (now - entry.getValue() > ALIVE_TIME) {
                String key = entry.getKey();
                instanceLastTime.remove(key);
                instanceMap.remove(key);
                dynamicPoolSnapshotMap.remove(key);
            }
        }
    }

    public String getService(String serviceName, String host, Integer port) {
        return String.format("%s@%s:%d", serviceName, host, port);
    }

    public String getService(String serviceName, ServiceInstance instance) {
        return getService(serviceName, instance.getHost(), instance.getPort());
    }

    public Collection<DynamicPoolSnapshot> getDynamicPoolSnapshotByService(String service) {
        String prefix = service + "@";
        return dynamicPoolSnapshotMap.entrySet().stream()
                .filter(e -> e.getKey().startsWith(prefix))
                .flatMap(e -> e.getValue()
                        .stream().peek(tpsna -> tpsna.setService(service)))
                .collect(Collectors.toList());
    }

    public Collection<DynamicPoolSnapshot> getDynamicPoolSnapshotByService(String service, String host, Integer port) {
        Set<DynamicPoolSnapshot> set = dynamicPoolSnapshotMap.get(getService(service, host, port));
        return set == null ? Collections.emptyList() :
                set.stream().peek(tpsna -> tpsna.setService(service)).collect(Collectors.toList());
    }
}

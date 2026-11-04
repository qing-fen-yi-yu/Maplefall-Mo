package com.guilu.Listener;

import cn.hutool.core.lang.TypeReference;
import com.guilu.domain.DynamicPoolSnapshot;
import com.guilu.utils.JsonUtils;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.discovery.event.HeartbeatEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import com.guilu.constants.DynamicTPMetaKey;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class TPConfigListener {
    @Getter
    Map<String, List<DynamicPoolSnapshot>> dynamicPoolSnapshotMap = new ConcurrentHashMap<>();
    private final DiscoveryClient discoveryClient;

    public TPConfigListener(DiscoveryClient discoveryClient) {
        this.discoveryClient = discoveryClient;
        onHeartbeatEvent();
    }
    @EventListener(HeartbeatEvent.class)
    public void onHeartbeatEvent() {
        try {
            for (String service : discoveryClient.getServices()) {
                for (ServiceInstance instance : discoveryClient.getInstances(service)) {
                    Map<String, String> metadata = instance.getMetadata();
                        String dynamicTpStr = metadata.get(DynamicTPMetaKey.DYNAMIC_TP_KEY);
                        Map<String, DynamicPoolSnapshot> reportTPs =
                                JsonUtils.toBean(dynamicTpStr, new TypeReference<Map<String, DynamicPoolSnapshot>>() {});
                    if(reportTPs == null){
                        log.warn("线程池配置中心拉取{}，{}\t服务线程池数据为空", service, instance.getUri());
                        continue;
                    }
                    dynamicPoolSnapshotMap.put(service, reportTPs.values().stream().toList());
                    log.info("拉取 {} 成功，合并: {}", service, reportTPs);
                }
            }
        } catch (Exception e) {
            log.error("线程池配置中心拉取服务线程池数据失败", e);
        }
    }
    public List<DynamicPoolSnapshot> getDynamicPoolSnapshot(String service) {
        return dynamicPoolSnapshotMap.get(service);
    }
}

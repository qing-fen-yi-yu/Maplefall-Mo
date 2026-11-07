package com.guilu.instance;

import com.guilu.constants.DynamicTPMetaKey;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

//实例管理器
@Slf4j
public class InstanceManager {
    @Getter
    private final String curAppName;

    private static final long ALIVE_TIME = 60000L;

    private final DiscoveryClient discoveryClient;
    private final List<AbstractInstanceManager> instanceManagers = new ArrayList<>();
    private final Map<String, Long> instanceLastTime = new ConcurrentHashMap<>();
    @Getter
    private final Map<String, ServiceInstance> instanceMap = new ConcurrentHashMap<>();

    public InstanceManager(DiscoveryClient discoveryClient, Environment environment) {
        this.curAppName = environment.getProperty("spring.application.name");
        this.discoveryClient = discoveryClient;
    }

    @Scheduled(cron = "*/30 * * * * *")
    public void scanInstance(){
        initInstance();
        cleanExpired();
    }
    public void initInstance() {
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

    //当每次清理实例与注册时都会调用该方法
    public void cleanExpired() {
        List<String> removeInstances = new ArrayList<>();
        long now = System.currentTimeMillis();
        for (Map.Entry<String, Long> entry : instanceLastTime.entrySet()) {
            if (now - entry.getValue() > ALIVE_TIME) {
                String key = entry.getKey();
                instanceLastTime.remove(key);
                instanceMap.remove(key);
                removeInstances.add(key);
            }
        }
        instanceManagers.forEach(instanceManager -> instanceManager.clear(removeInstances));
    }

    public String getService(String serviceName, String host, Integer port) {
        return String.format("%s@%s:%d", serviceName, host, port);
    }

    public String getService(String serviceName, ServiceInstance instance) {
        return getService(serviceName, instance.getHost(), instance.getPort());
    }

    public List<ServiceInstance> getInstance(String service) {
        List<ServiceInstance> instances = new ArrayList<>();
        for(String instanceKey : instanceMap.keySet()){
            if(instanceKey.startsWith(service)){
                instances.add(instanceMap.get(instanceKey));
            }
        }
        return instances;
    }
    public void register(AbstractInstanceManager instanceManager) {
        instanceManagers.add(instanceManager);
    }
}

package com.guilu.config;

import com.guilu.instance.InstanceManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Slf4j
@Configuration
public class InstanceConfiguration {
    @Bean
    public InstanceManager instanceManager(DiscoveryClient discoveryClient, Environment environment) {
        return new InstanceManager(discoveryClient, environment);
    }
//    @Bean
//    @ConditionalOnProperty(name = "web.dispatch.instance.scan.enabled", matchIfMissing = true, havingValue = "true")
//    public InstanceScanJob instanceScanJob(InstanceManager instanceManager) {
//        return new InstanceScanJob(instanceManager);
//    }
}

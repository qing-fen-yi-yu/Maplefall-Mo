package com.guilu.config;

import com.guilu.instance.InstanceManager;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
public class InstanceConfiguration {
    @Bean
    public InstanceManager instanceManager(DiscoveryClient discoveryClient, Environment environment) {
        return new InstanceManager(discoveryClient, environment);
    }
}

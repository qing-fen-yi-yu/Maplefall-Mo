package com.guilu.config;

import com.guilu.Manager.updater.PoolUpdater;
import com.guilu.client.TPUpdaterClient;
import com.guilu.client.Web.WebDispatchClient;
import com.guilu.instance.InstanceManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.concurrent.Semaphore;

@Configuration
public class ClientConfiguration {
    @Bean("TPUpdaterClient")
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix="monitor.threadpool.dynamic", name="enabled", havingValue="true")
    public TPUpdaterClient tpUpdaterClient(InstanceManager instanceManager, PoolUpdater poolUpdater, WebDispatchClient webDispatchClient, @Value("${server.servlet.context-path}") String prefix) {
        return new TPUpdaterClient(instanceManager, poolUpdater, webDispatchClient, prefix);
    }
    @Bean
    public WebClient webClient(WebClient.Builder builder) {
        return builder
                .clientConnector(new ReactorClientHttpConnector(
                        HttpClient.create().responseTimeout(Duration.ofSeconds(3))))
                .build();
    }
    @Bean
    public WebDispatchClient webDispatchClient(WebClient webClient, @Value("${web.dispatch.semaphore.size:100}") int semaphoreSize) {
        return new WebDispatchClient(webClient, new Semaphore(semaphoreSize));
    }
}

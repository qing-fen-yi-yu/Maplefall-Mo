package com.guilu.config;

import io.github.mweirauch.micrometer.jvm.extras.ProcessMemoryMetrics;
import io.github.mweirauch.micrometer.jvm.extras.ProcessThreadMetrics;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

/**
 * JVM 扩展指标（进程内存 / 进程线程）
 */
@Slf4j
@AutoConfiguration
@ConditionalOnClass(ProcessMemoryMetrics.class)
@ConditionalOnProperty(prefix = "monitor.jvm.extras", name = "enabled",
        havingValue = "true", matchIfMissing = true)
public class JvmExtrasAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public ProcessMemoryMetrics processMemoryMetrics() {
        return new ProcessMemoryMetrics();
    }

    @Bean
    @ConditionalOnMissingBean
    public ProcessThreadMetrics processThreadMetrics() {
        return new ProcessThreadMetrics();
    }

    @PostConstruct
    public void init(){
        log.info("jvm扩展信息点监控");
    }
}

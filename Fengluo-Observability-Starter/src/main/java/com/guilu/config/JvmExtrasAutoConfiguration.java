package com.guilu.config;

import io.github.mweirauch.micrometer.jvm.extras.ProcessMemoryMetrics;
import io.github.mweirauch.micrometer.jvm.extras.ProcessThreadMetrics;
import io.micrometer.core.instrument.binder.MeterBinder;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

/**
 * JVM扩展
 */
@AutoConfiguration
@ConditionalOnClass(ProcessMemoryMetrics.class)
@ConditionalOnProperty(prefix = "monitor.jvm.extras", name = "enabled",
        havingValue = "true", matchIfMissing = false)
public class JvmExtrasAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public MeterBinder processMemoryMetrics() {
        return new ProcessMemoryMetrics();
    }

    @Bean
    @ConditionalOnMissingBean
    public MeterBinder processThreadMetrics() {
        return new ProcessThreadMetrics();
    }
}

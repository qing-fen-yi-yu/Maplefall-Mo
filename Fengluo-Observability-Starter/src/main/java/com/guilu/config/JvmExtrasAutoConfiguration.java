package com.guilu.config;

import io.github.mweirauch.micrometer.jvm.extras.ProcessMemoryMetrics;
import io.github.mweirauch.micrometer.jvm.extras.ProcessThreadMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmInfoMetrics;
import org.springframework.boot.actuate.autoconfigure.metrics.JvmMetricsAutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;


@AutoConfiguration(after = JvmMetricsAutoConfiguration.class)
@ConditionalOnClass({ProcessMemoryMetrics.class, ProcessThreadMetrics.class})
@ConditionalOnProperty(prefix = "monitor.jvm.extras", name = "enabled", havingValue = "true", matchIfMissing = true)
public class JvmExtrasAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean
    public ProcessMemoryMetrics processMemoryMetrics() { return new ProcessMemoryMetrics(); }

    @Bean
    @ConditionalOnMissingBean
    public ProcessThreadMetrics processThreadMetrics() { return new ProcessThreadMetrics(); }

    @Bean
    @ConditionalOnMissingBean
    public JvmInfoMetrics jvmInfoMetrics() { return new JvmInfoMetrics(); }
}

package com.guilu.config;

import com.guilu.Manager.reporter.PoolReporter;
import com.guilu.monitor.sdk.ExecutorPoolMetrics;
import com.guilu.monitor.sdk.GcEventMetrics;
import com.guilu.monitor.sdk.JvmSupplementMetrics;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.*;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;


/**
 * 指标注册器
 */
@AutoConfiguration(after = DynamicPoolAutoConfiguration.class)
@EnableConfigurationProperties(MonitorMetricsProperties.class)
@ConditionalOnClass({MeterRegistry.class, JvmSupplementMetrics.class})
@ConditionalOnProperty(prefix = "monitor.metrics", name = "enabled", havingValue = "true", matchIfMissing = true)
public class MonitorMetricsAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "monitor.metrics",
            name = "jvm-enabled",
            havingValue = "true", matchIfMissing = true)
    public JvmSupplementMetrics fengluoJvmMetrics() { return new JvmSupplementMetrics(); }

    @Bean(destroyMethod = "close")
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "monitor.metrics", name = "gc-enabled",
            havingValue = "true", matchIfMissing = true)
    public GcEventMetrics fengluoGcMetrics() { return new GcEventMetrics(); }

    @Bean(destroyMethod = "close")
    @ConditionalOnBean(PoolReporter.class)
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "monitor.metrics",
            name = "executor-enabled", havingValue = "true", matchIfMissing = true)
    public ExecutorPoolMetrics fengluoExecutorMetrics(PoolReporter reporter,
                      MonitorMetricsProperties properties) {
        return new ExecutorPoolMetrics(reporter::snapshot, properties.getExecutorRefreshInterval());
    }
}

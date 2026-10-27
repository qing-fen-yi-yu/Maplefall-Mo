package com.guilu.config;

import com.guilu.Manager.reporter.PoolReporter;
import com.guilu.metrics.PoolReporterSnapshotSource;
import com.guilu.monitor.sdk.GcMetricsBinder;
import com.guilu.monitor.sdk.JvmSupplementMetrics;
import com.guilu.monitor.sdk.spi.ThreadPoolSnapshotSource;
import com.guilu.properties.MonitorMetricsProperties;
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
    public JvmSupplementMetrics fengluoJvmMetrics(MonitorMetricsProperties properties) {
        return new JvmSupplementMetrics(properties.getJvmRefreshInterval(),
                properties.isSysDetailInfo());
    }

    @Bean(destroyMethod = "close")
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "monitor.metrics", name = "gc-enabled",
            havingValue = "true", matchIfMissing = true)
    public GcMetricsBinder fengluoGcMetrics() {
        return new GcMetricsBinder();
    }

    @Bean
    @ConditionalOnClass(PoolReporter.class)
    @ConditionalOnBean(PoolReporter.class)
    @ConditionalOnMissingBean(ThreadPoolSnapshotSource.class)
    @ConditionalOnProperty(prefix = "monitor.metrics",
            name = "executor-enabled", havingValue = "true", matchIfMissing = true)
    public ThreadPoolSnapshotSource fengluoThreadPoolSnapshotSource(PoolReporter reporter) {
        return new PoolReporterSnapshotSource(reporter);
    }
}

package com.guilu.config;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import com.guilu.monitor.sdk.binder.logging.enums.LoggingMetricType;
import com.guilu.monitor.sdk.binder.logging.AbstractLoggingBinder;
import com.guilu.monitor.sdk.binder.logging.CustomerAppender;
import com.guilu.monitor.sdk.binder.logging.DefaultMetricsBinder;
import com.guilu.monitor.sdk.binder.logging.ExpandMetricsBinder;
import com.guilu.properties.MonitorMetricsProperties;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;

//日志配置器
@AutoConfiguration
@EnableConfigurationProperties(MonitorMetricsProperties.class)
@ConditionalOnClass(MeterRegistry.class)
@ConditionalOnProperty(prefix = "monitor.logging", name = "enabled",
        havingValue = "true", matchIfMissing = true)
public class LoggingMetricsAutoConfiguration implements Ordered {

    @Bean
    @ConditionalOnMissingBean(AbstractLoggingBinder.class)
    public AbstractLoggingBinder loggingBinder(MonitorMetricsProperties props) {
        LoggingMetricType type = props.getMetricType();
        AbstractLoggingBinder binder = null;
        if (type == LoggingMetricType.EXPAND) {
            binder = new ExpandMetricsBinder();
        } else {
            binder = new DefaultMetricsBinder();
        }
        return binder;
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}

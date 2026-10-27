package com.guilu.config;

import io.github.mweirauch.micrometer.jvm.extras.ProcessMemoryMetrics;
import io.github.mweirauch.micrometer.jvm.extras.ProcessThreadMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmInfoMetrics;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@ConditionalOnProperty(prefix = "monitor.jvm.extras", name = "enabled", havingValue = "true", matchIfMissing = true)
public class JvmExtrasAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnClass(ProcessMemoryMetrics.class)
    @ConditionalOnExpression("#{'${os.name:}'.toLowerCase().contains('linux')}")
    public ProcessMemoryMetrics processMemoryMetrics() {
        return new ProcessMemoryMetrics();
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnClass(ProcessThreadMetrics.class)
    @ConditionalOnExpression("#{'${os.name:}'.toLowerCase().contains('linux')}")
    public ProcessThreadMetrics processThreadMetrics() {
        return new ProcessThreadMetrics();
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnClass(JvmInfoMetrics.class)
    public JvmInfoMetrics jvmInfoMetrics() {
        return new JvmInfoMetrics();
    }
}
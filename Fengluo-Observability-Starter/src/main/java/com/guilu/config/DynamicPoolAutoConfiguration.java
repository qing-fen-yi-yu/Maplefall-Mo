package com.guilu.config;

import com.guilu.Manager.PoolLifecycle;
import com.guilu.Manager.inquirer.PoolInquirer;
import com.guilu.Manager.register.PoolRegister;
import com.guilu.Manager.register.RegisterBeanPostProcessor;
import com.guilu.Manager.reporter.PoolReporter;
import com.guilu.Manager.updater.PoolUpdater;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

@AutoConfiguration
@ConditionalOnProperty(prefix="monitor.threadpool.dynamic", name="enabled", havingValue="true")
public class DynamicPoolAutoConfiguration {
    @Bean
    public static RegisterBeanPostProcessor dynamicPoolRegistrar(Environment env, ConfigurableListableBeanFactory factory) {
        return new RegisterBeanPostProcessor(env, factory);
    }
    @Bean(name={"ThreadPoolUpdater", "dynamicPoolUpdater"})
    public PoolUpdater dynamicPoolUpdater() { return PoolUpdater.of(); }
    @Bean(name={"ThreadPoolInquirer", "dynamicPoolInquirer"})
    public PoolInquirer dynamicPoolInquirer() { return PoolInquirer.of(); }
    @Bean(name={"ThreadPoolReporter", "dynamicPoolReporter"})
    public PoolReporter dynamicPoolReporter(Environment environment) { return new PoolReporter(environment); }
    @Bean(name={"PoolInitAndDes", "dynamicPoolLifecycle"})
    public PoolLifecycle dynamicPoolLifecycle() { return new PoolLifecycle(); }
    @Bean(name={"ThreadPoolRegister", "dynamicPoolRegister"})
    public PoolRegister dynamicPoolRegister() { return new PoolRegister(); }
}

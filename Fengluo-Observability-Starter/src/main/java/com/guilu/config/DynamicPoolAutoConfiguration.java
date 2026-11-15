package com.guilu.config;

import com.alibaba.cloud.nacos.NacosDiscoveryProperties;
import com.alibaba.cloud.nacos.registry.NacosRegistration;
import com.alibaba.cloud.nacos.registry.NacosServiceRegistry;
import com.guilu.DynamicTPConfig.TpConfigUpdater;
import com.guilu.DynamicTPConfig.TpMetaDataReporter;
import com.guilu.Manager.PoolLifecycle;
import com.guilu.Manager.inquirer.PoolInquirer;
import com.guilu.Manager.register.PoolRegister;
import com.guilu.Manager.register.RegisterBeanPostProcessor;
import com.guilu.Manager.reporter.PoolReporter;
import com.guilu.Manager.reporter.TPReporter;
import com.guilu.Manager.updater.AutoDestroyTP;
import com.guilu.Manager.updater.PoolUpdater;
import com.guilu.Manager.updater.TPUpdater;
import com.guilu.Manager.updater.UpdaterListener;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.env.Environment;

@AutoConfiguration
@ConditionalOnProperty(prefix="monitor.threadpool.dynamic", name="enabled", havingValue="true")
public class DynamicPoolAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean
    public static RegisterBeanPostProcessor dynamicPoolRegistrar(Environment env, ConfigurableListableBeanFactory factory) {
        return new RegisterBeanPostProcessor(env, factory);
    }
    @Bean(name={"ThreadPoolUpdater", "dynamicPoolUpdater"})
    @ConditionalOnMissingBean
    public PoolUpdater dynamicPoolUpdater(DefaultListableBeanFactory beanFactory) {
        return new PoolUpdater(beanFactory);
    }
    @Bean(name={"ThreadPoolInquirer", "dynamicPoolInquirer"})
    @ConditionalOnMissingBean
    public PoolInquirer dynamicPoolInquirer() {
        return PoolInquirer.of();
    }
    @Bean(name={"ThreadPoolReporter", "dynamicPoolReporter"})
    @ConditionalOnMissingBean
    public PoolReporter dynamicPoolReporter(Environment environment) {
        return new PoolReporter(environment);
    }
    @Bean(name={"PoolInitAndDes", "dynamicPoolLifecycle"})
    @ConditionalOnMissingBean
    public PoolLifecycle dynamicPoolLifecycle(AutoDestroyTP autoDestroyTP) {
        return new PoolLifecycle(autoDestroyTP);
    }
    @Bean
    @ConditionalOnMissingBean
    public AutoDestroyTP autoDestroyTP(PoolUpdater poolUpdater) {
        return new AutoDestroyTP(poolUpdater);
    }
    @Bean(name={"ThreadPoolRegister", "dynamicPoolRegister"})
    @ConditionalOnMissingBean
    public PoolRegister dynamicPoolRegister() {
        return new PoolRegister();
    }
    @Bean("ThreadPoolConfigUpdateListener")
    @ConditionalOnMissingBean
    public UpdaterListener updaterListener(PoolUpdater poolUpdater) {
        return new UpdaterListener(poolUpdater);
    }
    @Bean
    @ConditionalOnMissingBean
    public TPReporter tpMetaDataReporter(PoolReporter poolReporter,
                                         NacosDiscoveryProperties properties,
                                         NacosServiceRegistry serviceRegistry, NacosRegistration registration) {
        return new TpMetaDataReporter(poolReporter, properties, serviceRegistry, registration);
    }
    @Bean
    @ConditionalOnMissingBean
    public TPUpdater tpConfigUpdater(ApplicationEventPublisher publisher, PoolUpdater poolUpdater){
        return new TpConfigUpdater(publisher, poolUpdater);
    }
}

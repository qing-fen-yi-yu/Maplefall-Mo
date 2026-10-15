package com.guilu.config;

import com.guilu.Register.PoolRegister;
import com.guilu.Register.RegisterBeanPostProcessor;
import io.github.mweirauch.micrometer.jvm.extras.ProcessMemoryMetrics;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@AutoConfiguration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnClass(RestControllerAdvice.class)
@ConditionalOnProperty(prefix = "monitor.threadpool.dynamic", name = "enabled",
        havingValue = "true", matchIfMissing = true)
public class ThreadRegisterConfiguration {
    @Bean
    public BeanPostProcessor threadRegister(Environment env, ConfigurableListableBeanFactory factory) {
        return new RegisterBeanPostProcessor(env,factory);
    }
    @Bean
    @ConditionalOnClass(ProcessMemoryMetrics.class)
    public PoolRegister poolManager(){
        return new PoolRegister();
    }
}

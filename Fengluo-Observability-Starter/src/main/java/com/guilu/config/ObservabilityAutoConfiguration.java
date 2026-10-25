package com.guilu.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@AutoConfiguration
public class ObservabilityAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "monitor.banner",
            name = "enabled", havingValue = "true", matchIfMissing = true)
    public OBInIt observabilityBanner() {
        return new OBInIt();
    }
}
@Slf4j
class OBInIt implements ApplicationRunner {
    @Override
    public void run(ApplicationArguments args) throws IOException {
        try (InputStream input = new ClassPathResource("OBBanner.txt").getInputStream()) {
            String banner = new String(input.readAllBytes(), StandardCharsets.UTF_8);
            log.info("\n{}",banner);
        }
    }
}


package com.guilu;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableAsync;

import java.net.InetAddress;
import java.net.UnknownHostException;

@Slf4j
@EnableAsync
@EnableCaching
@SpringBootApplication
public class TaskApplication {
    public static void main(String[] args) {
        SpringApplication app = new SpringApplicationBuilder(TaskApplication.class).build();
        Environment env = app.run(args).getEnvironment();
        String protocol = "http";
        if (env.getProperty("server.ssl.key-store") != null) {
            protocol = "https";
        }
        try {
            log.info("--/\n---------------------------------------------------------------------------------------\n\t" +
                            "Application '{}' is running! Access URLs:\n\t" +
                            "Local: \t\t{}://localhost:{}\n\t" +
                            "External: \t{}://{}:{}\n\t" +
                            "ManagementPoint:\t{}"+
                            "Profile(s): \t{}" +
                            "\n---------------------------------------------------------------------------------------",
                    env.getProperty("spring.application.name"),
                    protocol,
                    env.getProperty("server.port"),
                    protocol,
                    InetAddress.getLocalHost().getHostAddress(),
                    env.getProperty("server.port"),
                    env.getProperty("management.endpoints.web.exposure.include"),
                    env.getActiveProfiles());
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }
}

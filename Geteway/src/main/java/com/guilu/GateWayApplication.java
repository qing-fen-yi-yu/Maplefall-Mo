package com.guilu;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.core.env.Environment;

@Slf4j
@SpringBootApplication
public class GateWayApplication {
    public static void main(String[] args) {
        SpringApplication app = new SpringApplicationBuilder(GateWayApplication.class).build();
        app.run(args);
        Environment env = app.run(args).getEnvironment();
        log.info("启动成功");
    }
}

package com.guilu.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.io.*;

@Slf4j
@Component
public class OBInIt implements ApplicationRunner {
    @Override
    public void run(ApplicationArguments args) throws IOException {
        File file = new File("E:\\FengLuo\\Fengluo-Observability-Starter\\src\\main\\resources\\OBBanner.txt");
        FileInputStream fileInputStream = new FileInputStream(file);
        try {
            String banner = new String(fileInputStream.readAllBytes());
            log.info("\n{}",banner);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            fileInputStream.close();
        }
    }
}

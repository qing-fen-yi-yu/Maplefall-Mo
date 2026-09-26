package com.guilu.config;

import com.guilu.service.PrivilegeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * 启动时把权限配置发布到 Redis（auth:privileges），
 * 使网关在收到第一个请求前就有可用的权限缓存。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PrivilegeCacheInitializer implements ApplicationRunner {

    private final PrivilegeService privilegeService;

    @Override
    public void run(ApplicationArguments args) {
        try {
            privilegeService.publish();
            log.info("权限缓存初始化完成");
        } catch (Exception e) {
            log.error("权限缓存初始化失败，本次启动不阻断；权限变更后请重新发布", e);
        }
    }
}

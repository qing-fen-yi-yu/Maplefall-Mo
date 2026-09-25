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
 * <p>
 * 发布失败只记录日志、不阻断启动：Redis 不可用时 {@code AuthUtil} 会保留上一次的缓存，
 * 未配置权限的路径依然放行，而 token 的有效性校验不受影响。
 * 权限数据变更后应由业务侧再次调用 {@link PrivilegeService#publish()}。
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

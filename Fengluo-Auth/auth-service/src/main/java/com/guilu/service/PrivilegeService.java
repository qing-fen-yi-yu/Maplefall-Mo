package com.guilu.service;

import cn.hutool.json.JSONUtil;
import com.guilu.access.PrivilegeAccessor;
import com.guilu.domain.dto.PrivilegeRoleDTO;
import com.guilu.utils.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.guilu.constants.JwtConstants.AUTH_PRIVILEGE_KEY;
import static com.guilu.constants.JwtConstants.AUTH_PRIVILEGE_VERSION_KEY;
import static com.guilu.constants.JwtConstants.LOCK_AUTH_PRIVILEGE_KEY;

/**
 * 权限缓存发布：把数据源中的权限配置写入 Redis（auth:privileges），
 * 并递增版本号通知各节点刷新。由拥有权限数据的模块在权限变更后调用，
 * 网关只读取缓存，不访问数据库。
 */
@Slf4j
@RequiredArgsConstructor
public class PrivilegeService {

    private static final Duration LOCK_TTL = Duration.ofSeconds(10);

    private final PrivilegeAccessor privilegeAccessor;
    private final StringRedisTemplate stringRedisTemplate;

    /**
     * 重新加载权限并写入 Redis 缓存（多实例下用分布式锁避免重复发布）。
     */
    public void publish() throws InterruptedException {
        Boolean locked = stringRedisTemplate.opsForValue()
                .setIfAbsent(LOCK_AUTH_PRIVILEGE_KEY, "1", LOCK_TTL);
        if (!Boolean.TRUE.equals(locked)) {
            log.debug("其他实例正在发布权限缓存，跳过本次发布");
            return;
        }
        try {
            doPublish();
        } finally {
            stringRedisTemplate.delete(LOCK_AUTH_PRIVILEGE_KEY);
        }
    }

    private void doPublish() {
        List<PrivilegeRoleDTO> all = privilegeAccessor.loadAll();
        Map<String, String> desired = new HashMap<>();
        if (all != null) {
            for (PrivilegeRoleDTO dto : all) {
                if (dto != null && StringUtils.isNotBlank(dto.getAntPath())) {
                    desired.put(dto.getAntPath(), JSONUtil.toJsonStr(dto));
                }
            }
        }
        BoundHashOperations<String, String, String> hashOps = stringRedisTemplate.boundHashOps(AUTH_PRIVILEGE_KEY);
        Map<String, String> current = hashOps.entries();
        // 1.删除已不存在的权限项，避免删除-重建期间出现空缓存的放行窗口
        for (String field : current.keySet()) {
            if (!AUTH_PRIVILEGE_VERSION_KEY.equals(field) && !desired.containsKey(field)) {
                hashOps.delete(field);
            }
        }
        // 2.写入新增/变更的权限项
        if (!desired.isEmpty()) {
            hashOps.putAll(desired);
        }
        // 3.最后递增版本号，通知各节点刷新
        hashOps.put(AUTH_PRIVILEGE_VERSION_KEY, String.valueOf(nextVersion(current)));
        log.debug("权限缓存发布完成, 共 {} 条", desired.size());
    }

    private int nextVersion(Map<String, String> current) {
        String value = current.get(AUTH_PRIVILEGE_VERSION_KEY);
        if (StringUtils.isBlank(value)) {
            return 1;
        }
        try {
            return Integer.parseInt(value.trim()) + 1;
        } catch (NumberFormatException e) {
            return 1;
        }
    }
}

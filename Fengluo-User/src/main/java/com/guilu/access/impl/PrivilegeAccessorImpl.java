package com.guilu.access.impl;

import com.guilu.access.PrivilegeAccessor;
import com.guilu.domain.dto.PrivilegePathRow;
import com.guilu.domain.dto.PrivilegeRoleDTO;
import com.guilu.mapper.SysMenuMapper;
import com.guilu.utils.CollUtils;
import com.guilu.utils.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

/**
 * 权限数据访问实现：从 fl_auth 库读取"路径 -> 允许访问的角色"。
 * <p>
 * 数据来源为 sys_menu.permission 经 sys_role_menu 关联到 sys_role，
 * 注册为 bean 后 {@code PrivilegeAutoConfiguration} 才会创建 {@code PrivilegeService}，
 * 由它在权限变更后把结果发布到 Redis 供网关读取。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PrivilegeAccessorImpl implements PrivilegeAccessor {

    private final SysMenuMapper sysMenuMapper;

    @Override
    public List<PrivilegeRoleDTO> loadAll() {
        List<PrivilegePathRow> rows = sysMenuMapper.selectPrivileges();
        if (CollUtils.isEmpty(rows)) {
            log.debug("未查询到任何权限配置，权限缓存将为空（所有路径均不做角色校验）");
            return CollUtils.emptyList();
        }
        // 一行一个 (路径, 角色)，同一路径的多行合并为一个 DTO
        Map<String, PrivilegeRoleDTO> merged = new LinkedHashMap<>();
        for (PrivilegePathRow row : rows) {
            String antPath = StringUtils.trimToNull(row.getPermission());
            if (antPath == null || row.getRoleId() == null) {
                continue;
            }
            PrivilegeRoleDTO dto = merged.computeIfAbsent(antPath, path -> {
                PrivilegeRoleDTO created = new PrivilegeRoleDTO();
                created.setId(row.getMenuId());
                created.setAntPath(path);
                created.setRoles(new LinkedHashSet<>());
                return created;
            });
            dto.getRoles().add(row.getRoleId());
        }
        log.debug("加载权限配置完成, 路径 {} 条", merged.size());
        return new ArrayList<>(merged.values());
    }
}

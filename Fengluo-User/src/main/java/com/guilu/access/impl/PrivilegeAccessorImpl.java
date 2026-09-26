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
            log.debug("未查询到任何权限配置，权限缓存将为空");
            return CollUtils.emptyList();
        }
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

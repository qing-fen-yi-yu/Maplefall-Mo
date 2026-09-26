package com.guilu.access;

import com.guilu.domain.dto.PrivilegeRoleDTO;

import java.util.List;

/**
 * 权限数据访问接口
 */
public interface PrivilegeAccessor {

    /**
     * 加载全部权限配置。
     *
     * @return 权限列表，无数据时返回空集合
     */
    List<PrivilegeRoleDTO> loadAll();
}

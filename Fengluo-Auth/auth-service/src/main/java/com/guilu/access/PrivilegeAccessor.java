package com.guilu.access;

import com.guilu.domain.dto.PrivilegeRoleDTO;

import java.util.List;

/**
 * 权限数据访问接口（路径 -> 允许访问的角色）。
 * <p>
 * 由拥有权限表的业务模块提供实现；Auth 模块只负责把数据缓存到 Redis 并做校验，
 * 网关只读缓存、不访问数据库。业务侧实现完成后注册为 bean 即可生效。
 */
public interface PrivilegeAccessor {

    /**
     * 加载全部权限配置。
     *
     * @return 权限列表，无数据时返回空集合
     */
    List<PrivilegeRoleDTO> loadAll();
}

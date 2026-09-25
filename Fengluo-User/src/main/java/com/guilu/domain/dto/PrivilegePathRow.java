package com.guilu.domain.dto;

import lombok.Data;

/**
 * sys_menu ⋈ sys_role_menu 的查询结果行：一条 (路径, 角色) 授权关系。
 * 由 {@link com.guilu.mapper.SysMenuMapper#selectPrivileges()} 返回，
 * 在 {@code PrivilegeAccessorImpl} 中按路径聚合为 {@code PrivilegeRoleDTO}。
 */
@Data
public class PrivilegePathRow {

    /** 菜单/权限ID */
    private Long menuId;

    /** 网关匹配用的路径表达式 */
    private String permission;

    /** 允许访问该路径的角色ID */
    private Long roleId;
}

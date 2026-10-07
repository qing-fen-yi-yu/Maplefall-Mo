package com.guilu.constants;

/**
 * 角色模型常量。
 * <p>
 * 本项目的角色模型是<b>单角色</b>：token 里只承载一个 roleId（见 {@code LoginUserDTO.roleId}），
 * 由 {@code sys_user_role} 经 {@code ORDER BY r.sort ASC LIMIT 1} 解析。
 */
public interface RoleConstants {

    /**
     * 默认角色ID：sys_role 中预置的「普通用户」。
     * 用户注册时默认绑定该角色，角色映射执行降级时也回到该角色。
     * 注意 0 是一个<b>真实存在的角色主键</b>，不是「无角色」的空值语义。
     */
    long DEFAULT_ROLE_ID = 0L;

    /** 角色/用户状态：正常 */
    int STATUS_ENABLED = 1;

    /** 角色/用户状态：禁用 */
    int STATUS_DISABLED = 0;
}

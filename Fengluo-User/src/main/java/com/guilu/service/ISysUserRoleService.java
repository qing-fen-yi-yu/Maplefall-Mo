package com.guilu.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.guilu.domain.dto.UserRoleRequest;
import com.guilu.domain.dto.query.UserRoleQuery;
import com.guilu.domain.po.SysUserRole;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 用户-角色关系 服务类
 * </p>
 * <p>
 * 角色模型是单角色：token 内只承载一个 roleId，因此「新增绑定」「改绑」「降级」
 * 本质上都是「保留一个角色、清掉该用户的其它绑定」。
 * </p>
 *
 * @author 归鹭
 * @since 2026-09-25
 */
public interface ISysUserRoleService extends IService<SysUserRole> {

    Long selectPrimaryRoleId(Long userId);

    Page<SysUserRole> pageQuery(UserRoleQuery userRoleQuery);

    /**
     * 把用户的角色设为指定角色，会清掉该用户原有的其它角色绑定。
     * 角色必须存在且处于启用状态。
     */
    void assignRole(Long userId, Long roleId);

    /**
     * 批量设置用户的角色，整批在同一事务内完成。
     */
    void assignRoleBatch(List<UserRoleRequest> requests);

    void addUserRole(UserRoleRequest userRoleRequest);

    void updateUserRole(UserRoleRequest userRoleRequest);

    /**
     * 绑定默认角色（普通用户）。注册流程使用：不校验默认角色是否存在，
     * 避免默认角色数据缺失时连注册都不可用，缺失只记录告警。
     */
    void bindDefaultRole(Long userId);

    /**
     * 降级为普通用户：保留默认角色，清掉其它角色绑定。
     */
    void removeByUserId(Long userId);

    /**
     * 逻辑删除用户的全部角色绑定，供删除用户时清理。
     */
    void removeAllByUserId(Long userId);
}

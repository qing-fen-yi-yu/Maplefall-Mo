package com.guilu.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.guilu.domain.dto.RoleMenuRequest;
import com.guilu.domain.dto.query.RoleQuery;
import com.guilu.domain.dto.update.RoleUpdateRequest;
import com.guilu.domain.po.SysRole;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 角色表 服务类
 * </p>
 *
 * @author 归鹭
 * @since 2026-09-25
 */
public interface ISysRoleService extends IService<SysRole> {

    void insertRole(SysRole sysRole);

    Page<SysRole> pageQuery(RoleQuery roleQuery);

    SysRole getRoleDetail(Long id);

    void updateRole(RoleUpdateRequest request);

    void updateStatus(Long id, Integer status);

    /**
     * 删除角色。仍被用户绑定的角色、以及默认角色不允许删除。
     */
    void removeRole(Long id);

    /**
     * 查询角色已绑定的菜单/权限ID。
     */
    List<Long> listMenuIds(Long roleId);

    /**
     * 用全量集合覆盖角色的菜单授权，空集合表示清空。
     */
    void replaceMenus(RoleMenuRequest request);

    /**
     * 清空角色的全部菜单授权。
     */
    void clearMenus(Long roleId);

    /**
     * 重新发布权限缓存（Redis auth:privileges），让网关立即生效。
     * <p>
     * 必须在事务<b>提交之后</b>调用：publish 会用当前线程的连接重新读库，
     * 若在事务内执行，写入缓存的是未提交的快照。
     * </p>
     */
    void publishPrivileges();
}

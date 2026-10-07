package com.guilu.mapper;

import com.guilu.domain.po.SysRoleMenu;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.List;

/**
 * <p>
 * 角色-权限关系表 Mapper 接口
 * </p>
 *
 * @author 归鹭
 * @since 2026-09-25
 */
public interface SysRoleMenuMapper extends BaseMapper<SysRoleMenu> {

    /**
     * 查询角色已绑定的菜单/权限ID。
     */
    List<Long> selectMenuIdsByRoleId(@Param("roleId") Long roleId);

    /**
     * 绑定角色与菜单。同 {@link SysUserRoleMapper#upsertUserRole}：
     * 唯一索引 uk_sys_role_menu 不排除已删除行，用 ON DUPLICATE KEY UPDATE 复活旧行，
     * 避免「取消授权后重新授权同一权限」冲突。
     *
     * @param id     主键，实体主键策略是 ASSIGN_ID，需调用方生成
     * @param roleId 角色ID
     * @param menuId 菜单/权限ID
     * @return 影响行数
     */
    int upsertRoleMenu(@Param("id") Long id,
                       @Param("roleId") Long roleId,
                       @Param("menuId") Long menuId);

    /**
     * 逻辑删除角色在给定集合之外的绑定，用于「全量覆盖」授权。
     * menuIds 为空集合表示清空该角色的全部授权。
     *
     * @param roleId  角色ID
     * @param menuIds 需要保留的菜单/权限ID，可为空集合
     * @return 影响行数
     */
    int softDeleteNotIn(@Param("roleId") Long roleId,
                        @Param("menuIds") Collection<Long> menuIds);

    /**
     * 逻辑删除角色的全部授权，用于角色被删除时的清理。
     */
    int softDeleteByRoleId(@Param("roleId") Long roleId);
}

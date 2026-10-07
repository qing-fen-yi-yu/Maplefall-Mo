package com.guilu.mapper;

import com.guilu.domain.po.SysUserRole;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

/**
 * <p>
 * 用户-角色关系 Mapper 接口
 * </p>
 *
 * @author 归鹭
 * @since 2026-09-25
 */
public interface SysUserRoleMapper extends BaseMapper<SysUserRole> {

    /**
     * 取用户登录时写入 token 的单一角色：启用状态、未逻辑删除、sort 最小的那个。
     * token 内只承载一个 roleId，因此多角色用户的其余角色不参与网关鉴权。
     *
     * @param userId 用户ID
     * @return 角色ID，用户未绑定启用角色时返回 null
     */
    Long selectPrimaryRoleId(@Param("userId") Long userId);

    /**
     * 绑定用户与角色。若该 (user_id, role_id) 曾被逻辑删除，普通 insert 会因为
     * uk_sys_user_role 唯一索引不排除已删除行而冲突，这里用 ON DUPLICATE KEY UPDATE
     * 把旧行复活，使「解绑后重新绑定同一角色」成为幂等操作。
     *
     * @param id     主键。实体主键策略是 ASSIGN_ID、表上无自增，必须由调用方生成
     * @param userId 用户ID
     * @param roleId 角色ID
     * @return 影响行数
     */
    int upsertUserRole(@Param("id") Long id,
                       @Param("userId") Long userId,
                       @Param("roleId") Long roleId);

    /**
     * 逻辑删除用户在指定角色之外的其余绑定。单角色模型下「改绑」与「降级」都是
     * 「保留一个角色、清掉其它」，因此共用这一条语句。
     *
     * @param userId     用户ID
     * @param keepRoleId 要保留的角色ID
     * @return 影响行数
     */
    int softDeleteOtherRoles(@Param("userId") Long userId,
                             @Param("keepRoleId") Long keepRoleId);
}

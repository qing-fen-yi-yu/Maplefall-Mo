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
}

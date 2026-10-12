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
     * 绑定角色与菜单
     */
    int upsertRoleMenu(@Param("id") Long id,
                       @Param("roleId") Long roleId,
                       @Param("menuId") Long menuId);

    int deleteNotIn(@Param("roleId") Long roleId,
                        @Param("menuIds") Collection<Long> menuIds);

    int deleteByRoleId(@Param("roleId") Long roleId);

    boolean saveRoleMenuAll(@Param("menuId") Long menuId,@Param("roleIds") List<Long> roleId);

    void deleteRoleAndMenu(@Param("menuId") Long menuId, @Param("roleId") Collection<Long> roleId);

    boolean deleteMenuIds(@Param("menuIds") List<Long> menuId);
}

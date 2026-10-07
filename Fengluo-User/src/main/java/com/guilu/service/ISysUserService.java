package com.guilu.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.guilu.domain.dto.query.UserQuery;
import com.guilu.domain.dto.update.UserCreateRequest;
import com.guilu.domain.dto.update.UserUpdateRequest;
import com.guilu.domain.po.SysUser;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 系统用户 服务类
 * </p>
 *
 * @author 归鹭
 * @since 2026-09-25
 */
public interface ISysUserService extends IService<SysUser> {

    Page<SysUser> queryUser(UserQuery userQuery);

    /**
     * 校验用户名、邮箱未被占用。用户名与邮箱在 sys_user 上都是唯一索引。
     *
     * @param username  用户名，为空则跳过
     * @param email     邮箱，为空则跳过
     * @param excludeId 需要排除的用户ID：更新资料时传自身ID，新增时传 null
     */
    void assertNotRegistered(String username, String email, Long excludeId);

    /**
     * 查询用户详情，密码摘要会被置空后再返回。
     */
    SysUser getDetail(Long id);

    /**
     * 管理端新增用户，并在同一事务内绑定角色（默认角色或请求指定角色）。
     */
    void createUser(UserCreateRequest request);

    /**
     * 管理端修改用户资料。只覆盖请求中显式给出的字段，不接收密码与状态。
     */
    void updateUser(UserUpdateRequest request);

    /**
     * 启用/停用用户。不允许把当前登录账号自己停用。
     */
    void updateStatus(Long id, Integer status);

    /**
     * 重置指定用户的密码。
     * <p>
     * 入参是<b>明文</b>新密码，加密在本方法内完成：调用方无法把明文直接写库。
     * 供管理端重置密码与用户修改本人密码复用。
     * </p>
     */
    void resetPassword(Long userId, String rawPassword);

    /**
     * 批量删除用户，连同其角色映射一起逻辑删除。
     */
    void removeBatch(List<Long> ids);
}

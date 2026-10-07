package com.guilu.service.impl;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.guilu.constants.ResultInfo;
import com.guilu.domain.dto.UserRoleRequest;
import com.guilu.domain.dto.query.UserRoleQuery;
import com.guilu.domain.po.SysRole;
import com.guilu.domain.po.SysUserRole;
import com.guilu.domain.query.QueryPage;
import com.guilu.exception.RequestException.BadRequestException;
import com.guilu.mapper.SysUserRoleMapper;
import com.guilu.service.ISysRoleService;
import com.guilu.service.ISysUserRoleService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.guilu.utils.AssertUtils;
import com.guilu.utils.queryUtil.QueryCreateUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.util.List;

import static com.guilu.constants.ErrConstants.COMMON.ID_REQUIRED;
import static com.guilu.constants.ErrConstants.ROLE.DEFAULT_ROLE_MAPPING_FORBIDDEN;
import static com.guilu.constants.ErrConstants.ROLE.ROLE_DISABLED;
import static com.guilu.constants.ErrConstants.ROLE.ROLE_NOT_EXISTS;
import static com.guilu.constants.RoleConstants.DEFAULT_ROLE_ID;
import static com.guilu.constants.RoleConstants.STATUS_ENABLED;

/**
 * <p>
 * 用户-角色关系 服务实现类
 * </p>
 *
 * @author 归鹭
 * @since 2026-09-25
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysUserRoleServiceImpl extends ServiceImpl<SysUserRoleMapper, SysUserRole> implements ISysUserRoleService {

    private final QueryCreateUtils<SysUserRole> wrapperCreateUtils = new QueryCreateUtils<>();

    private final ISysRoleService roleService;

    @Override
    public Long selectPrimaryRoleId(Long userId) {
        return getBaseMapper().selectPrimaryRoleId(userId);
    }

    @Override
    public Page<SysUserRole> pageQuery(UserRoleQuery userRoleQuery) {
        AssertUtils.isNotNull(userRoleQuery, ResultInfo.Msg.REQUEST_PARAM_ILLEGAL);
        QueryPage p = userRoleQuery.getPage();
        return page(new Page<>(p.getCurrent(), p.getPageSize()),
                wrapperCreateUtils.generateQueryWrapper(userRoleQuery));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignRole(Long userId, Long roleId) {
        AssertUtils.isNotNull(userId, "用户ID禁止为空");
        assertRoleAssignable(roleId);
        // 单角色模型：先摘掉该用户的其它角色，再绑定目标角色
        baseMapper.softDeleteOtherRoles(userId, roleId);
        baseMapper.upsertUserRole(IdWorker.getId(), userId, roleId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignRoleBatch(List<UserRoleRequest> requests) {
        AssertUtils.isNotEmpty(requests, ResultInfo.Msg.REQUEST_PARAM_ILLEGAL);
        for (UserRoleRequest request : requests) {
            AssertUtils.isNotNull(request, ResultInfo.Msg.REQUEST_PARAM_ILLEGAL);
            assignRole(request.getUserId(), request.getRoleId());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addUserRole(UserRoleRequest userRoleRequest) {
        AssertUtils.isNotNull(userRoleRequest, ResultInfo.Msg.REQUEST_PARAM_ILLEGAL);
        assignRole(userRoleRequest.getUserId(), userRoleRequest.getRoleId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUserRole(UserRoleRequest userRoleRequest) {
        addUserRole(userRoleRequest);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void bindDefaultRole(Long userId) {
        AssertUtils.isNotNull(userId, "用户ID禁止为空");
        if (!roleService.exists(Wrappers.<SysRole>lambdaQuery().eq(SysRole::getId, DEFAULT_ROLE_ID))) {
            log.warn("默认角色 {} 不存在，用户 {} 绑定后不会获得任何权限", DEFAULT_ROLE_ID, userId);
        }
        baseMapper.softDeleteOtherRoles(userId, DEFAULT_ROLE_ID);
        baseMapper.upsertUserRole(IdWorker.getId(), userId, DEFAULT_ROLE_ID);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeByUserId(Long userId) {
        AssertUtils.isNotNull(userId, "用户ID禁止为空");
        assertRoleAssignable(DEFAULT_ROLE_ID);
        baseMapper.softDeleteOtherRoles(userId, DEFAULT_ROLE_ID);
        baseMapper.upsertUserRole(IdWorker.getId(), userId, DEFAULT_ROLE_ID);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeAllByUserId(Long userId) {
        AssertUtils.isNotNull(userId, "用户ID禁止为空");
        remove(Wrappers.<SysUserRole>lambdaQuery().eq(SysUserRole::getUserId, userId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removeById(Serializable id) {
        AssertUtils.isNotNull(id, ID_REQUIRED);
        SysUserRole row = getById(id);
        // 默认角色是用户的兜底角色
        if (row != null && Long.valueOf(DEFAULT_ROLE_ID).equals(row.getRoleId())) {
            throw new BadRequestException(DEFAULT_ROLE_MAPPING_FORBIDDEN);
        }
        return super.removeById(id);
    }

    /**
     * 角色必须存在且启用才可分配。实时查库：角色新增/停用后立即生效。
     */
    private void assertRoleAssignable(Long roleId) {
        AssertUtils.isNotNull(roleId, "角色ID禁止为空");
        SysRole role = roleService.getById(roleId);
        AssertUtils.isNotNull(role, ROLE_NOT_EXISTS);
        AssertUtils.isTrue(role.getStatus() != null && role.getStatus() == STATUS_ENABLED, ROLE_DISABLED);
    }
}

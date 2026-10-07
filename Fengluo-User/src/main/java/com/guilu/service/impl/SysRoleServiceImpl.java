package com.guilu.service.impl;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.guilu.constants.ResultInfo;
import com.guilu.domain.dto.RoleMenuRequest;
import com.guilu.domain.dto.query.RoleQuery;
import com.guilu.domain.dto.update.RoleUpdateRequest;
import com.guilu.domain.po.SysRole;
import com.guilu.domain.po.SysUserRole;
import com.guilu.domain.query.QueryPage;
import com.guilu.exception.BusinessException.CommonException;
import com.guilu.exception.RequestException.BadRequestException;
import com.guilu.mapper.SysMenuMapper;
import com.guilu.mapper.SysRoleMapper;
import com.guilu.mapper.SysRoleMenuMapper;
import com.guilu.mapper.SysUserRoleMapper;
import com.guilu.service.ISysRoleService;
import com.guilu.service.PrivilegeService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.guilu.utils.AssertUtils;
import com.guilu.utils.CollUtils;
import com.guilu.utils.StringUtils;
import com.guilu.utils.queryUtil.QueryCreateUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.guilu.constants.ErrConstants.COMMON.ID_REQUIRED;
import static com.guilu.constants.ErrConstants.COMMON.INVALID_STATUS;
import static com.guilu.constants.ErrConstants.ROLE.DEFAULT_ROLE_DISABLE_FORBIDDEN;
import static com.guilu.constants.ErrConstants.ROLE.DEFAULT_ROLE_FORBIDDEN;
import static com.guilu.constants.ErrConstants.ROLE.MENU_NOT_EXISTS;
import static com.guilu.constants.ErrConstants.ROLE.PRIVILEGE_PUBLISH_FAILED;
import static com.guilu.constants.ErrConstants.ROLE.ROLE_CODE_ALREADY_EXISTS;
import static com.guilu.constants.ErrConstants.ROLE.ROLE_CODE_REQUIRED;
import static com.guilu.constants.ErrConstants.ROLE.ROLE_IN_USE;
import static com.guilu.constants.ErrConstants.ROLE.ROLE_NAME_REQUIRED;
import static com.guilu.constants.ErrConstants.ROLE.ROLE_NOT_EXISTS;
import static com.guilu.constants.RoleConstants.DEFAULT_ROLE_ID;
import static com.guilu.constants.RoleConstants.STATUS_DISABLED;
import static com.guilu.constants.RoleConstants.STATUS_ENABLED;

/**
 * <p>
 * 角色表 服务实现类
 * </p>
 *
 * @author 归鹭
 * @since 2026-09-25
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysRoleServiceImpl extends ServiceImpl<SysRoleMapper, SysRole> implements ISysRoleService {

    private final QueryCreateUtils<SysRole> wrapperCreateUtils = new QueryCreateUtils<>();

    private final SysRoleMenuMapper roleMenuMapper;
    private final SysUserRoleMapper userRoleMapper;
    private final SysMenuMapper menuMapper;
    private final PrivilegeService privilegeService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void insertRole(SysRole sysRole) {
        AssertUtils.isNotNull(sysRole, ResultInfo.Msg.REQUEST_PARAM_ILLEGAL);
        String roleCode = StringUtils.trimToNull(sysRole.getRoleCode());
        AssertUtils.isNotBlank(roleCode, ROLE_CODE_REQUIRED);
        String roleName = StringUtils.trimToNull(sysRole.getRoleName());
        AssertUtils.isNotBlank(roleName, ROLE_NAME_REQUIRED);
        if (exists(Wrappers.<SysRole>lambdaQuery().eq(SysRole::getRoleCode, roleCode))) {
            throw new BadRequestException(ROLE_CODE_ALREADY_EXISTS);
        }
        LocalDateTime now = LocalDateTime.now();
        // 按白名单重建实体，避免请求体里的 id / deleted / 时间戳被直接落库
        SysRole entity = new SysRole()
                .setRoleCode(roleCode)
                .setRoleName(roleName)
                .setDescription(sysRole.getDescription())
                .setSort(sysRole.getSort() == null ? 0 : sysRole.getSort())
                .setStatus(isValidStatus(sysRole.getStatus()) ? sysRole.getStatus() : STATUS_ENABLED)
                .setDeleted(0)
                .setCreatedAt(now)
                .setUpdatedAt(now);
        try {
            save(entity);
        } catch (DuplicateKeyException e) {
            throw new BadRequestException(ROLE_CODE_ALREADY_EXISTS);
        }
    }

    @Override
    public Page<SysRole> pageQuery(RoleQuery roleQuery) {
        AssertUtils.isNotNull(roleQuery, ResultInfo.Msg.REQUEST_PARAM_ILLEGAL);
        QueryPage p = roleQuery.getPage();
        return page(new Page<>(p.getCurrent(), p.getPageSize()),
                wrapperCreateUtils.generateQueryWrapper(roleQuery));
    }

    @Override
    public SysRole getRoleDetail(Long id) {
        AssertUtils.isNotNull(id, ID_REQUIRED);
        SysRole role = getById(id);
        AssertUtils.isNotNull(role, ROLE_NOT_EXISTS);
        return role;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateRole(RoleUpdateRequest request) {
        AssertUtils.isNotNull(request, ResultInfo.Msg.REQUEST_PARAM_ILLEGAL);
        Long id = request.getId();
        AssertUtils.isNotNull(id, ID_REQUIRED);
        AssertUtils.isNotNull(getById(id), ROLE_NOT_EXISTS);
        String roleName = StringUtils.trimToNull(request.getRoleName());
        // roleCode 不在可改范围：sys_role 上有唯一约束，改编码收益低、冲突成本高
        boolean updated = update(Wrappers.<SysRole>lambdaUpdate()
                .eq(SysRole::getId, id)
                .set(roleName != null, SysRole::getRoleName, roleName)
                .set(request.getDescription() != null, SysRole::getDescription, request.getDescription())
                .set(request.getSort() != null, SysRole::getSort, request.getSort())
                .set(SysRole::getUpdatedAt, LocalDateTime.now()));
        AssertUtils.isTrue(updated, ResultInfo.Msg.DB_UPDATE_EXCEPTION);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(Long id, Integer status) {
        AssertUtils.isNotNull(id, ID_REQUIRED);
        AssertUtils.isTrue(isValidStatus(status), INVALID_STATUS);
        AssertUtils.isNotNull(getById(id), ROLE_NOT_EXISTS);
        // 默认角色是注册与降级的兜底：停用后 selectPrimaryRoleId 会过滤掉它，
        // 结果是新注册用户拿到无角色的 token（处处被拒），降级接口也会抛错
        if (Long.valueOf(DEFAULT_ROLE_ID).equals(id) && status != STATUS_ENABLED) {
            throw new BadRequestException(DEFAULT_ROLE_DISABLE_FORBIDDEN);
        }
        boolean updated = update(Wrappers.<SysRole>lambdaUpdate()
                .eq(SysRole::getId, id)
                .set(SysRole::getStatus, status)
                .set(SysRole::getUpdatedAt, LocalDateTime.now()));
        AssertUtils.isTrue(updated, ResultInfo.Msg.DB_UPDATE_EXCEPTION);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeRole(Long id) {
        AssertUtils.isNotNull(id, ID_REQUIRED);
        AssertUtils.isNotNull(getById(id), ROLE_NOT_EXISTS);
        if (Long.valueOf(DEFAULT_ROLE_ID).equals(id)) {
            throw new BadRequestException(DEFAULT_ROLE_FORBIDDEN);
        }
        Long bound = userRoleMapper.selectCount(Wrappers.<SysUserRole>lambdaQuery()
                .eq(SysUserRole::getRoleId, id));
        if (bound != null && bound > 0) {
            // 直接删会让已绑定的用户静默失去角色，先要求人工解绑
            throw new BadRequestException(ROLE_IN_USE);
        }
        roleMenuMapper.softDeleteByRoleId(id);
        removeById(id);
    }

    @Override
    public List<Long> listMenuIds(Long roleId) {
        AssertUtils.isNotNull(roleId, ID_REQUIRED);
        AssertUtils.isNotNull(getById(roleId), ROLE_NOT_EXISTS);
        List<Long> menuIds = roleMenuMapper.selectMenuIdsByRoleId(roleId);
        return menuIds == null ? CollUtils.emptyList() : menuIds;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void replaceMenus(RoleMenuRequest request) {
        AssertUtils.isNotNull(request, ResultInfo.Msg.REQUEST_PARAM_ILLEGAL);
        Long roleId = request.getRoleId();
        AssertUtils.isNotNull(roleId, ID_REQUIRED);
        AssertUtils.isNotNull(getById(roleId), ROLE_NOT_EXISTS);
        List<Long> menuIds = request.getMenuIds() == null
                ? CollUtils.emptyList()
                : request.getMenuIds().stream()
                        .filter(Objects::nonNull)
                        .distinct()
                        .collect(Collectors.toList());
        assertMenusExist(menuIds);
        // 全量覆盖：先摘掉不在集合里的旧授权（空集合即清空），再把集合内的绑定复活/新建
        roleMenuMapper.softDeleteNotIn(roleId, menuIds);
        for (Long menuId : menuIds) {
            roleMenuMapper.upsertRoleMenu(IdWorker.getId(), roleId, menuId);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void clearMenus(Long roleId) {
        AssertUtils.isNotNull(roleId, ID_REQUIRED);
        AssertUtils.isNotNull(getById(roleId), ROLE_NOT_EXISTS);
        roleMenuMapper.softDeleteByRoleId(roleId);
    }

    @Override
    public void publishPrivileges() {
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            throw new IllegalStateException("publishPrivileges 必须在事务提交之后调用");
        }
        try {
            privilegeService.publish();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("权限缓存发布被中断", e);
            throw new CommonException(PRIVILEGE_PUBLISH_FAILED);
        } catch (RuntimeException e) {
            log.error("权限缓存发布失败", e);
            throw new CommonException(PRIVILEGE_PUBLISH_FAILED);
        }
    }

    /**
     * 不存在的菜单ID会造成「授权成功但实际不生效」，这里直接拒绝。
     */
    private void assertMenusExist(List<Long> menuIds) {
        if (CollUtils.isEmpty(menuIds)) {
            return;
        }
        if (menuMapper.selectByIds(menuIds).size() != menuIds.size()) {
            throw new BadRequestException(MENU_NOT_EXISTS);
        }
    }

    private boolean isValidStatus(Integer status) {
        return status != null && (status == STATUS_ENABLED || status == STATUS_DISABLED);
    }
}

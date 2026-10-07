package com.guilu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.guilu.constants.ResultInfo;
import com.guilu.domain.dto.query.UserQuery;
import com.guilu.domain.dto.update.UserCreateRequest;
import com.guilu.domain.dto.update.UserUpdateRequest;
import com.guilu.domain.po.SysUser;
import com.guilu.domain.query.QueryPage;
import com.guilu.exception.GlobalException;
import com.guilu.exception.RequestException.BadRequestException;
import com.guilu.mapper.SysUserMapper;
import com.guilu.service.ISysUserRoleService;
import com.guilu.service.ISysUserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.guilu.util.PasswordEncoder;
import com.guilu.utils.AssertUtils;
import com.guilu.utils.BeanUtils;
import com.guilu.utils.StringUtils;
import com.guilu.utils.UserContext;
import com.guilu.utils.queryUtil.QueryCreateUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

import static com.guilu.constants.ErrConstants.ACCOUNT.EMAIL_ALREADY_EXISTS;
import static com.guilu.constants.ErrConstants.ACCOUNT.REGISTER_CONFLICT;
import static com.guilu.constants.ErrConstants.ACCOUNT.USERNAME_ALREADY_EXISTS;
import static com.guilu.constants.ErrConstants.COMMON.ID_REQUIRED;
import static com.guilu.constants.ErrConstants.COMMON.INVALID_STATUS;
import static com.guilu.constants.ErrConstants.USER.PASSWORD_REQUIRED;
import static com.guilu.constants.ErrConstants.USER.SELF_DELETE_FORBIDDEN;
import static com.guilu.constants.ErrConstants.USER.SELF_STATUS_FORBIDDEN;
import static com.guilu.constants.ErrConstants.USER.USERNAME_REQUIRED;
import static com.guilu.constants.RoleConstants.STATUS_DISABLED;
import static com.guilu.constants.RoleConstants.STATUS_ENABLED;

/**
 * <p>
 * 系统用户 服务实现类
 * </p>
 *
 * @author 归鹭
 * @since 2026-09-25
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements ISysUserService {

    private final QueryCreateUtils<SysUser> wrapperCreateUtils = new QueryCreateUtils<>();

    private final ISysUserRoleService userRoleService;

    @Override
    public Page<SysUser> queryUser(UserQuery userQuery) {
        AssertUtils.isNotNull(userQuery, ResultInfo.Msg.REQUEST_PARAM_ILLEGAL);
        QueryWrapper<SysUser> sysUserQueryWrapper = wrapperCreateUtils.generateQueryWrapper(userQuery);
        sysUserQueryWrapper.select(SysUser.class, info -> !"password_hash".equals(info.getColumn()));
        QueryPage pDto = userQuery.getPage();
        return page(new Page<>(pDto.getCurrent(), pDto.getPageSize()), sysUserQueryWrapper);
    }

    @Override
    public void assertNotRegistered(String username, String email, Long excludeId) {
        if (StringUtils.isNotBlank(username) && exists(Wrappers.<SysUser>lambdaQuery()
                .eq(SysUser::getUsername, username)
                .ne(excludeId != null, SysUser::getId, excludeId))) {
            throw new BadRequestException(USERNAME_ALREADY_EXISTS);
        }
        if (StringUtils.isNotBlank(email) && exists(Wrappers.<SysUser>lambdaQuery()
                .eq(SysUser::getEmail, email)
                .ne(excludeId != null, SysUser::getId, excludeId))) {
            throw new BadRequestException(EMAIL_ALREADY_EXISTS);
        }
    }

    @Override
    public SysUser getDetail(Long id) {
        AssertUtils.isNotNull(id, ID_REQUIRED);
        SysUser user = getById(id);
        AssertUtils.isNotNull(user, ResultInfo.Msg.USER_NOT_EXISTS);
        // 密码摘要不对外暴露
        user.setPassword(null);
        return user;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createUser(UserCreateRequest request) {
        AssertUtils.isNotNull(request, ResultInfo.Msg.REQUEST_PARAM_ILLEGAL);
        String username = StringUtils.trimToNull(request.getUsername());
        AssertUtils.isNotBlank(username, USERNAME_REQUIRED);
        AssertUtils.isNotBlank(request.getPassword(), PASSWORD_REQUIRED);
        String email = StringUtils.trimToNull(request.getEmail());
        assertNotRegistered(username, email, null);

        LocalDateTime now = LocalDateTime.now();
        SysUser user = BeanUtils.copyBean(request, SysUser.class);
        user.setId(null);
        user.setUsername(username);
        user.setNickname(StringUtils.isBlank(user.getNickname()) ? username : user.getNickname().trim());
        user.setEmail(email);
        // 覆盖请求里的明文密码，禁止明文落库
        user.setPassword(PasswordEncoder.encode(request.getPassword()));
        user.setStatus(STATUS_ENABLED);
        user.setSuperAdmin(0);
        user.setDeleted(0);
        user.setCreatedAt(now);
        user.setUpdatedAt(now);
        try {
            save(user);
        } catch (DuplicateKeyException e) {
            throw new BadRequestException(REGISTER_CONFLICT);
        }
        if (request.getRoleId() == null) {
            userRoleService.bindDefaultRole(user.getId());
        } else {
            userRoleService.assignRole(user.getId(), request.getRoleId());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUser(UserUpdateRequest request) {
        AssertUtils.isNotNull(request, ResultInfo.Msg.REQUEST_PARAM_ILLEGAL);
        Long id = request.getId();
        AssertUtils.isNotNull(id, ID_REQUIRED);
        SysUser exist = getById(id);
        AssertUtils.isNotNull(exist, ResultInfo.Msg.USER_NOT_EXISTS);

        String nickname = StringUtils.trimToNull(request.getNickname());
        String email = StringUtils.trimToNull(request.getEmail());
        if (email != null && !email.equals(exist.getEmail())) {
            assertNotRegistered(exist.getUsername(), email, id);
        }
        boolean updated = update(Wrappers.<SysUser>lambdaUpdate()
                .eq(SysUser::getId, id)
                .set(nickname != null, SysUser::getNickname, nickname)
                .set(email != null, SysUser::getEmail, email)
                .set(request.getPhone() != null, SysUser::getPhone, request.getPhone())
                .set(request.getAvatarUrl() != null, SysUser::getAvatarUrl, request.getAvatarUrl())
                .set(SysUser::getUpdatedAt, LocalDateTime.now()));
        AssertUtils.isTrue(updated, ResultInfo.Msg.DB_UPDATE_EXCEPTION);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(Long id, Integer status) {
        AssertUtils.isNotNull(id, ID_REQUIRED);
        AssertUtils.isTrue(isValidStatus(status), INVALID_STATUS);
        // 防止管理员把自己停用后再也登不进来
        if (id.equals(UserContext.getUser())) {
            throw new BadRequestException(SELF_STATUS_FORBIDDEN);
        }
        AssertUtils.isNotNull(getById(id), ResultInfo.Msg.USER_NOT_EXISTS);
        boolean updated = update(Wrappers.<SysUser>lambdaUpdate()
                .eq(SysUser::getId, id)
                .set(SysUser::getStatus, status)
                .set(SysUser::getUpdatedAt, LocalDateTime.now()));
        AssertUtils.isTrue(updated, ResultInfo.Msg.DB_UPDATE_EXCEPTION);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resetPassword(Long userId, String rawPassword) {
        AssertUtils.isNotNull(userId, ID_REQUIRED);
        AssertUtils.isNotBlank(rawPassword, PASSWORD_REQUIRED);
        boolean updated = update(Wrappers.<SysUser>lambdaUpdate()
                .eq(SysUser::getId, userId)
                .set(SysUser::getPassword, PasswordEncoder.encode(rawPassword))
                .set(SysUser::getUpdatedAt, LocalDateTime.now()));
        AssertUtils.isTrue(updated, ResultInfo.Msg.DB_UPDATE_EXCEPTION);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeBatch(List<Long> ids) {
        AssertUtils.isNotEmpty(ids, ResultInfo.Msg.REQUEST_PARAM_ILLEGAL);
        Long currentUserId = UserContext.getUser();
        for (Long id : ids) {
            AssertUtils.isNotNull(id, ID_REQUIRED);
            if (id.equals(currentUserId)) {
                throw new BadRequestException(SELF_DELETE_FORBIDDEN);
            }
            removeById(id);
        }
    }

    /**
     * 删除用户时同步逻辑删除其角色映射
     */
    @Override
    @Transactional(rollbackFor = GlobalException.class)
    public boolean removeById(Serializable id) {
        AssertUtils.isNotNull(id, ID_REQUIRED);
        userRoleService.removeAllByUserId(Long.valueOf(id.toString()));
        return super.removeById(id);
    }

    private boolean isValidStatus(Integer status) {
        return status != null && (status == STATUS_ENABLED || status == STATUS_DISABLED);
    }
}

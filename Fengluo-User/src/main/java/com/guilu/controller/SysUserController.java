package com.guilu.controller;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.guilu.domain.Result;
import com.guilu.domain.dto.query.UserQuery;
import com.guilu.domain.dto.update.ResetPasswordRequest;
import com.guilu.domain.dto.update.StatusUpdateRequest;
import com.guilu.domain.dto.update.UserCreateRequest;
import com.guilu.domain.dto.update.UserUpdateRequest;
import com.guilu.domain.po.SysUser;
import com.guilu.service.ISysUserService;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 * 系统用户 前端控制器
 * </p>
 *
 * @author 归鹭
 * @since 2026-09-25
 */
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class SysUserController {
    private final ISysUserService userService;

    @ApiOperation("用户分页查询")
    @PostMapping("/page/query")
    public Result<Page<SysUser>> queryUser(@RequestBody UserQuery userQuery){
        return Result.success(userService.queryUser(userQuery));
    }

    @ApiOperation("用户详情")
    @GetMapping("/detail/{id}")
    public Result<SysUser> detail(@PathVariable Long id) {
        return Result.success(userService.getDetail(id));
    }

    @ApiOperation("管理端新增用户")
    @PostMapping("/add")
    public Result<Void> addUser(@RequestBody UserCreateRequest userCreateRequest) {
        userService.createUser(userCreateRequest);
        return Result.success();
    }

    @ApiOperation("修改用户资料")
    @PutMapping("/update")
    public Result<Void> updateUser(@RequestBody UserUpdateRequest userUpdateRequest) {
        userService.updateUser(userUpdateRequest);
        return Result.success();
    }

    @ApiOperation("启用/停用用户")
    @PutMapping("/update/status")
    public Result<Void> updateStatus(@RequestBody StatusUpdateRequest statusUpdateRequest) {
        userService.updateStatus(statusUpdateRequest.getId(), statusUpdateRequest.getStatus());
        return Result.success();
    }

    @ApiOperation("重置用户密码")
    @PutMapping("/resetPassword")
    public Result<Void> resetPassword(@RequestBody ResetPasswordRequest resetPasswordRequest) {
        userService.resetPassword(resetPasswordRequest.getUserId(), resetPasswordRequest.getNewPassword());
        return Result.success();
    }

    @ApiOperation("批量删除用户")
    @DeleteMapping("/batch")
    public Result<Void> removeBatch(@RequestBody List<Long> ids) {
        userService.removeBatch(ids);
        return Result.success();
    }

    @ApiOperation("删除用户")
    @DeleteMapping("/del")
    public Result<Void> delUser(@RequestParam Long id) {
        userService.removeById(id);
        return Result.success();
    }
}

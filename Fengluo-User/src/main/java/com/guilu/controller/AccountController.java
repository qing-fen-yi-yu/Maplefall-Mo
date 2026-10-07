package com.guilu.controller;

import com.guilu.constants.JwtConstants;
import com.guilu.domain.Enum.CodeTypeEnum;
import com.guilu.domain.Result;
import com.guilu.domain.dto.LoginRequest;
import com.guilu.domain.dto.RefreshTokenRequest;
import com.guilu.domain.dto.RegisterRequest;
import com.guilu.domain.dto.TokenPair;
import com.guilu.domain.dto.update.PasswordUpdateRequest;
import com.guilu.domain.dto.update.UserInfo;
import com.guilu.domain.vo.ImageCodeVO;
import com.guilu.exception.RequestException.UnauthorizedException;
import com.guilu.service.AccountService;
import com.guilu.utils.UserContext;
import io.swagger.annotations.ApiOperation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class AccountController {
    private final AccountService accountService;

    @PostMapping("/register")
    public Result<TokenPair> register(@Valid @RequestBody RegisterRequest regisUser, HttpServletRequest request){
        return  Result.success(accountService.registerUser(regisUser,request));
    }

    @PostMapping("/login")
    public Result<TokenPair> login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        return Result.success(accountService.login(request, httpRequest));
    }
    @PostMapping("/refresh")
    public Result<TokenPair> refresh(@RequestBody RefreshTokenRequest request) {
        return Result.success(accountService.refresh(request.getRefreshToken()));
    }
    @PostMapping("/logout")
    public Result<Void> logout(
            @RequestHeader(value = JwtConstants.AUTHORIZATION_HEADER, required = false) String accessToken,
            @RequestHeader(value = JwtConstants.REFRESH_HEADER, required = false) String refreshToken) {
        accountService.logout(accessToken, refreshToken);
        return Result.success();
    }
    @ApiOperation("申请图形验证码")
    @GetMapping("/applyCode")
    public Result<ImageCodeVO> applyCode(CodeTypeEnum codeTypeEnum){
        return Result.success(accountService.createCode(codeTypeEnum));
    }
    /**
     * 当前登录用户
     */
    @ApiOperation("获取当前用户Id")
    @GetMapping("/me")
    public Result<Long> me() {
        Long userId = UserContext.getUser();

        if (userId == null) {
            throw new UnauthorizedException("未登录");
        }
        return Result.success(userId);
    }
    @ApiOperation("更新我的用户信息")
    @PutMapping("/update/myInfo")
    public Result<Void> updateInfo(@RequestBody UserInfo userInfo,HttpServletRequest request){
        accountService.updateMyInfo(userInfo,request);
        return Result.success();
    }

    /**
     * 修改本人密码。已签发的 token 不会因此失效，仍在使用期内可用。
     */
    @ApiOperation("修改当前登录用户密码")
    @PutMapping("/password")
    public Result<Void> changePassword(@RequestBody PasswordUpdateRequest passwordUpdateRequest,
                                       HttpServletRequest request) {
        accountService.changePassword(passwordUpdateRequest, request);
        return Result.success();
    }
}

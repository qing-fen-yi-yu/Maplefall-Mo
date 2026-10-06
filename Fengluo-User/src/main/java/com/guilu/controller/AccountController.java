package com.guilu.controller;

import com.guilu.constants.JwtConstants;
import com.guilu.domain.Enum.CodeTypeEnum;
import com.guilu.domain.Result;
import com.guilu.domain.dto.LoginRequest;
import com.guilu.domain.dto.RefreshTokenRequest;
import com.guilu.domain.dto.RegisterRequest;
import com.guilu.domain.dto.TokenPair;
import com.guilu.domain.vo.ImageCodeVO;
import com.guilu.exception.RequestException.UnauthorizedException;
import com.guilu.service.AccountService;
import com.guilu.service.ISysUserOauthService;
import com.guilu.utils.UserContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.awt.*;

@Slf4j
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class AccountController {
    private final ISysUserOauthService oauthService;
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
    @GetMapping("/applyCode")
    public Result<ImageCodeVO> applyCode(CodeTypeEnum codeTypeEnum){
        return Result.success(accountService.createCode(codeTypeEnum));
    }
    /**
     * 当前登录用户
     */
    @GetMapping("/me")
    public Result<Long> me() {
        Long userId = UserContext.getUser();

        if (userId == null) {
            throw new UnauthorizedException("未登录");
        }
        return Result.success(userId);
    }
}

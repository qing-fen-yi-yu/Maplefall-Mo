package com.guilu.controller;

import com.guilu.constants.JwtConstants;
import com.guilu.domain.Result;
import com.guilu.domain.dto.LoginRequest;
import com.guilu.domain.dto.RefreshTokenRequest;
import com.guilu.domain.dto.TokenPair;
import com.guilu.service.AccountService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/account")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @PostMapping("/login")
    public Result<TokenPair> login(@RequestBody LoginRequest request, HttpServletRequest httpRequest) {
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
}

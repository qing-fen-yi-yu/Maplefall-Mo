package com.guilu.domain.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RegisterRequest {
    //验证码
    @NotNull(message = "验证码禁止为空")
    private String code;
    //用户名
    @NotNull(message = "用户名禁止为空")
    private String username;
    //密码
    @NotNull(message = "密码禁止为空")
    private String password;
    //电子邮件
    private String email;
    //电话号码
    private String phone;
}

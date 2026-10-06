package com.guilu.domain.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class LoginRequest extends  CodeRequest{
    @NotNull(message = "用户名禁止为空")
    private String username;
    @NotNull(message = "密码禁止为空")
    private String password;

    private Boolean rememberMe;
}

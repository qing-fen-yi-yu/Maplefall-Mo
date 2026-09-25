package com.guilu.domain.dto;

import lombok.Data;

@Data
public class LoginRequest {

    private String username;

    private String password;

    private Boolean rememberMe;
}

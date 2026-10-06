package com.guilu.domain.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CodeRequest {
    //验证码
    @NotNull(message = "验证码禁止为空")
    private String code;
    @NotNull
    private String codeId;
}

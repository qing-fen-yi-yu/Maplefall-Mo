package com.guilu.domain.dto.update;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;


@Data
public class ResetPasswordRequest {
    @ApiModelProperty(value = "用户ID", required = true)
    private Long userId;
    @ApiModelProperty(value = "新密码", required = true)
    private String newPassword;
}

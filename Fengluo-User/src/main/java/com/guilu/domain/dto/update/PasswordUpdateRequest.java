package com.guilu.domain.dto.update;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 修改本人密码。
 */
@Data
public class PasswordUpdateRequest {
    @ApiModelProperty("原密码")
    private String oldPassword;
    @ApiModelProperty("新密码")
    private String newPassword;
}

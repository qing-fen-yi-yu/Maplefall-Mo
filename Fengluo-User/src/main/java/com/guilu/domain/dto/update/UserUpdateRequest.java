package com.guilu.domain.dto.update;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class UserUpdateRequest {
    @ApiModelProperty(value = "用户ID", required = true)
    private Long id;
    @ApiModelProperty("用户显示名称")
    private String nickname;
    @ApiModelProperty("邮箱")
    private String email;
    @ApiModelProperty("手机号")
    private String phone;
    @ApiModelProperty("头像地址")
    private String avatarUrl;
}

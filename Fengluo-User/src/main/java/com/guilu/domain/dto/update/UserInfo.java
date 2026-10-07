package com.guilu.domain.dto.update;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class UserInfo {
    @ApiModelProperty(name = "用户名")
    private String username;
    @ApiModelProperty(name = "真实姓名")
    private String nickname;
    @ApiModelProperty(name = "邮箱地址")
    private String email;
    @ApiModelProperty("手机号")
    private String phone;
    @ApiModelProperty("头像")
    private String avatarUrl;

}

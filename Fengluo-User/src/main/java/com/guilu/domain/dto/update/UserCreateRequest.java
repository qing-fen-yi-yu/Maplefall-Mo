package com.guilu.domain.dto.update;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 管理端新增用户。
 */
@Data
public class UserCreateRequest {
    @ApiModelProperty(value = "登录用户名", required = true)
    private String username;
    @ApiModelProperty("用户显示名称，为空时取用户名")
    private String nickname;
    @ApiModelProperty("初始密码")
    private String password;
    @ApiModelProperty("邮箱")
    private String email;
    @ApiModelProperty("手机号")
    private String phone;
    @ApiModelProperty("初始角色ID，为空时绑定默认角色（普通用户）")
    private Long roleId;
}

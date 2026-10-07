package com.guilu.domain.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
public class RoleMenuRequest {
    @ApiModelProperty(value = "角色ID", required = true)
    private Long roleId;
    @ApiModelProperty("菜单/权限ID")
    private List<Long> menuIds;
}

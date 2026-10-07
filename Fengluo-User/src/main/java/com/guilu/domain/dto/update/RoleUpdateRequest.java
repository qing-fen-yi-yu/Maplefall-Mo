package com.guilu.domain.dto.update;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class RoleUpdateRequest {
    @ApiModelProperty(value = "角色ID", required = true)
    private Long id;
    @ApiModelProperty("角色名称")
    private String roleName;
    @ApiModelProperty("角色介绍")
    private String description;
    @ApiModelProperty("显示顺序")
    private Integer sort;
}

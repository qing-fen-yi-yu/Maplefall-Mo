package com.guilu.domain.dto.update;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class StatusUpdateRequest {
    @ApiModelProperty(value = "主键ID", required = true)
    private Long id;
    @ApiModelProperty(value = "状态：0禁用，1正常", required = true)
    private Integer status;
}

package com.guilu.domain.dto;

import io.swagger.annotations.ApiModelProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TPConfigSnapshotQueryDto {
    @NotBlank(message = "服务名称不能为空")
    private String serviceName;
    @ApiModelProperty("实例配置-ip:port")
    private String instanceConfig;
}

package com.guilu.domain.dto;

import com.guilu.domain.query.QueryPage;
import com.guilu.utils.queryUtil.QueryType;
import com.guilu.utils.queryUtil.annotations.FieldQuery;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = true)
@Data
public class TPConfigPageQueryDto extends QueryPage {
    @FieldQuery
    private Long id;
    @FieldQuery(queryType = QueryType.LIKE)
    private String poolName;
    @ApiModelProperty("服务名称")
    @FieldQuery(queryType = QueryType.LIKE)
    private String serviceName;
    private Integer enabled;
    @ApiModelProperty("操作人ID")
    @FieldQuery(queryName = {"createdBy","updatedBy"})
    private Long operatorId;
    @ApiModelProperty("配置的创建与修改事件范围")
    @FieldQuery(queryType = QueryType.GE,queryName = {"createdAt","updatedAt"})
    private LocalDateTime operStartTime;
    @ApiModelProperty("配置的创建与修改事件范围")
    @FieldQuery(queryType = QueryType.LE,queryName = {"createdAt","updatedAt"})
    private LocalDateTime operEndTime;
    @FieldQuery
    private Integer deleted;
}

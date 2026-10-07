package com.guilu.domain.dto.query;

import com.guilu.domain.query.QueryPage;
import com.guilu.utils.queryUtil.QueryType;
import com.guilu.utils.queryUtil.annotations.FieldQuery;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 角色分页查询条件，写法与 {@link UserQuery} 一致。
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class RoleQuery extends QueryPage {
    @FieldQuery(queryName = "id")
    private Long id;
    @ApiModelProperty("角色编码，精确匹配")
    @FieldQuery(queryName = "role_code")
    private String roleCode;
    @ApiModelProperty("角色名称，模糊匹配")
    @FieldQuery(queryName = "role_name", queryType = QueryType.LIKE)
    private String roleName;
    @ApiModelProperty("角色状态：0禁用，1正常")
    @FieldQuery(queryName = "status")
    private Integer status;
    @ApiModelProperty("创建时间起始")
    @FieldQuery(queryName = "created_at", queryType = QueryType.GE)
    private Date startTime;
    @ApiModelProperty("创建时间截止")
    @FieldQuery(queryName = "created_at", queryType = QueryType.LE)
    private Date endTime;
}

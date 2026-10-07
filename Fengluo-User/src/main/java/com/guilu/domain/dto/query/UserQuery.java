package com.guilu.domain.dto.query;

import com.guilu.domain.query.QueryPage;
import com.guilu.utils.queryUtil.QueryType;
import com.guilu.utils.queryUtil.annotations.FieldQuery;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

@EqualsAndHashCode(callSuper = true)
@Data
public class UserQuery extends QueryPage {
    @FieldQuery(queryName = "id")
    private Long id;
    @FieldQuery(queryName = {"username","nickname"},queryType = QueryType.LIKE)
    private String name;
    @ApiModelProperty("登录Ip查询")
    @FieldQuery(queryName = "last_login_ip")
    private String loginIp;
    @ApiModelProperty("登录时间起始区间")
    @FieldQuery(queryName = "last_login_at",queryType = QueryType.GE)
    private Date loginStart;
    @ApiModelProperty("登陆时间结束时间")
    @FieldQuery(queryName = "last_login_at",queryType = QueryType.LE)
    private Date loginEnd;
    @ApiModelProperty(name = "最近的更新查询开始时间")
    @FieldQuery(queryName = "updated_at",queryType =  QueryType.GE)
    private Date startTime;
    @ApiModelProperty(name = "最近的更新查询结束时间")
    @FieldQuery(queryName = "updated_at",queryType = QueryType.LE)
    private Date endTime;
}

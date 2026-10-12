package com.guilu.domain.dto.query;

import com.guilu.domain.query.QueryPage;
import com.guilu.enums.MenuType;
import com.guilu.utils.queryUtil.QueryType;
import com.guilu.utils.queryUtil.annotations.FieldQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = true)
@Data
public class MenuQuery extends QueryPage {
    @FieldQuery
    private Long id;
    @FieldQuery
    private Long parentId;
    @FieldQuery
    private MenuType type;
    @FieldQuery
    private Integer status;
    @FieldQuery
    private Integer visible;
    @FieldQuery(queryName = "create_at",queryType = QueryType.GE)
    private LocalDateTime createStartTime;
    @FieldQuery(queryName = "create_at",queryType = QueryType.LE)
    private LocalDateTime createEndTime;
    @FieldQuery(queryName = "update_at",queryType = QueryType.GE)
    private LocalDateTime updateStartTime;
    @FieldQuery(queryName = "update_at",queryType =  QueryType.LE)
    private LocalDateTime updateEndTime;
}

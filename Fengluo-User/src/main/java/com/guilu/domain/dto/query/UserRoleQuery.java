package com.guilu.domain.dto.query;

import com.guilu.domain.query.QueryPage;
import com.guilu.utils.queryUtil.QueryType;
import com.guilu.utils.queryUtil.annotations.FieldQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

@EqualsAndHashCode(callSuper = true)
@Data
public class UserRoleQuery extends QueryPage {
    @FieldQuery
    private Long id;
    @FieldQuery(queryName = "user_id")
    private Long userId;
    @FieldQuery(queryName = "role_id")
    private Long roleId;
    @FieldQuery(queryName = "created_at", queryType = QueryType.GE)
    private Date createTime;
    /*
     * 这里刻意不提供 deleted 查询条件：全局逻辑删除已给所有 Wrapper 查询自动追加
     * deleted = 0，再暴露一个 deleted 条件既查不到已删除数据（会被 AND deleted = 0 抵消），
     * 又容易让调用方误以为可以查回收站。
     */
}

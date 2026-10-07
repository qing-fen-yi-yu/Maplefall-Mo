package com.guilu.domain.query;

import com.guilu.enums.PageSorted;
import com.guilu.utils.queryUtil.annotations.FieldQuery;
import lombok.Data;

@Data
public class QueryPage{
    /**
     * 当前页号
     */
    @FieldQuery(exits = false)
    private int current = 1;

    /**
     * 页面大小
     */
    @FieldQuery(exits = false)
    private int pageSize = 10;

    /// todo字段排序未实现
    /**
     * 排序字段
     */
    @FieldQuery(exits = false)
    private String sortField;
    /**
     * 排序--默认升序排序
     */
    @FieldQuery(exits = false)
    private PageSorted sort = PageSorted.ESC;

    public QueryPage getPage(){
        return this;
    }
}

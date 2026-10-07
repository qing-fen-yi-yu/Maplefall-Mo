package com.guilu.utils.queryUtil;

public enum QueryType {
    EQ, NE,
    GT, GE, LT, LE,
    LIKE, NOT_LIKE, LIKE_LEFT, LIKE_RIGHT,
    IN, NOT_IN,
    IS_NULL, IS_NOT_NULL,
    BETWEEN, NOT_BETWEEN,
//    ORDER_BY_ASC, ORDER_BY_DESC,   // 排序类，无值
//    GROUP_BY,                       // 分组，无值
//    EXISTS, NOT_EXISTS              // 子查询类
}

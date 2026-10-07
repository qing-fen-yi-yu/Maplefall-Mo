package com.guilu.utils.queryUtil.annotations;

import com.guilu.utils.queryUtil.QueryType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface FieldQuery {
    QueryType queryType() default QueryType.EQ;
    String[] queryName() default "";
    //是否进行扫描并拼接到查询sql中
    boolean exits() default true;
}

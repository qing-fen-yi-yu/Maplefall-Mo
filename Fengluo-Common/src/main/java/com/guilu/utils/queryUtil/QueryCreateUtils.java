package com.guilu.utils.queryUtil;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.guilu.exception.BusinessException.BadDataException;
import com.guilu.utils.queryUtil.annotations.FieldQuery;

import java.lang.reflect.Field;
import java.util.Collection;

public class QueryCreateUtils<T> extends  DefaultCreateUtils<T>{

    public QueryWrapper<T> generateQueryWrapper(Object tar) {
        QueryWrapper<T> qw = new QueryWrapper<>();
        if (tar == null) return qw;

        for (Class<?> c = tar.getClass(); c != null && c != Object.class; c = c.getSuperclass()) {
            for (Field field : c.getDeclaredFields()) {
                FieldQuery anno = field.getAnnotation(FieldQuery.class);
                if (anno == null) continue;
                if (!anno.exits()) continue;
                field.setAccessible(true);
                String[] sqlFields = anno.queryName();
                if (sqlFields.length == 0) {
                    //默认采用分段下划线命名方式
                    sqlFields = new String[]{toFiled(field.getName())};
                }
                byType(anno.queryType(), sqlFields, qw, field, tar);
            }
        }
        return qw;
    }
    private QueryWrapper<T> byType(QueryType type, String[] fields,
                                   QueryWrapper<T> qw, Field field, Object tar) {
        Object val;
        try {
            val = field.get(tar);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }

        for (String f : fields) {
            if (val == null
                    && type != QueryType.IS_NULL
                    && type != QueryType.IS_NOT_NULL) {
                break;
            }
            switch (type) {
                case EQ:          qw.eq(f, val);          break;
                case NE:          qw.ne(f, val);          break;
                case GT:          qw.gt(f, val);          break;
                case GE:          qw.ge(f, val);          break;
                case LT:          qw.lt(f, val);          break;
                case LE:          qw.le(f, val);          break;

                case LIKE:        qw.like(f, val);        break;
                case NOT_LIKE:    qw.notLike(f, val);     break;
                case LIKE_LEFT:   qw.likeLeft(f, val);    break;
                case LIKE_RIGHT:  qw.likeRight(f, val);   break;

                case IN:
                    if (val instanceof Collection || val.getClass().isArray()) {
                        qw.in(f, (Collection<?>) toCollection(val));
                    } else if (val instanceof String) {
                        String[] arr = ((String) val).split(",");
                        qw.in(f, (Object[]) arr);
                    } else {
                        qw.in(f, val);
                    }
                    break;
                case NOT_IN:
                    if (val instanceof Collection || val.getClass().isArray()) {
                        qw.notIn(f, (Collection<?>) toCollection(val));
                    } else if (val instanceof String) {
                        String[] arr = ((String) val).split(",");
                        qw.notIn(f, (Object[]) arr);
                    } else {
                        qw.notIn(f, val);
                    }
                    break;

                case IS_NULL:      qw.isNull(f);     break;
                case IS_NOT_NULL:  qw.isNotNull(f);  break;

                case BETWEEN:
                case NOT_BETWEEN:
                    // 值非 null 但形状不对属于调用方写错注解，直接报错而不是静默忽略
                    if (!(val instanceof Object[] range) || range.length != 2) {
                        throw new BadDataException("between类型需要值数量等于2");
                    }
                    if (type == QueryType.BETWEEN) {
                        qw.between(f, range[0], range[1]);
                    } else {
                        qw.notBetween(f, range[0], range[1]);
                    }
                    break;
                default: break;
            }
        }
        return qw;
    }
}
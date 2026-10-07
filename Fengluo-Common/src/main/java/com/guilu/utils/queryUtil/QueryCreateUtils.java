package com.guilu.utils.queryUtil;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.guilu.exception.BusinessException.BadDataException;
import com.guilu.exception.BusinessException.CommonException;
import com.guilu.utils.queryUtil.annotations.FieldQuery;

import java.lang.reflect.Field;
import java.util.Collection;

public class QueryCreateUtils<T> {

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

    private String toFiled(String name) {
        if (name == null || name.isEmpty()) return name;

        StringBuilder sb = new StringBuilder(name.length() + 8);
        char[] chars = name.toCharArray();

        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];
            if (Character.isUpperCase(c)) {
                //前一个是小写/数字 -> 直接插
                //前一个是大写、后一个是小写
                boolean prevExists     = i > 0;
                boolean prevNotUpper   = prevExists && !Character.isUpperCase(chars[i - 1]);
                boolean nextIsLower    = (i + 1 < chars.length) && Character.isLowerCase(chars[i + 1]);
                boolean prevIsUpper    = prevExists && Character.isUpperCase(chars[i - 1]);

                if (prevExists && (prevNotUpper || (prevIsUpper && nextIsLower))) {
                    sb.append('_');
                }
                sb.append(Character.toLowerCase(c));
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
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
            // 查询条件都是可选的：值为 null 表示调用方没有指定该条件，跳过即可。
            // IS_NULL / IS_NOT_NULL 本身不取值，val 为 null 时依然要生效。
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
                    if (!(val instanceof Object[]) || ((Object[]) val).length != 2) {
                        throw new BadDataException(
                                "@FieldQuery BETWEEN/NOT_BETWEEN 字段 [" + f + "] 需要长度=2的数组");
                    }
                    Object[] range = (Object[]) val;
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

    private Collection<?> toCollection(Object val) {
        if (val instanceof Collection) return (Collection<?>) val;
        if (val.getClass().isArray()) {
            int len = java.lang.reflect.Array.getLength(val);
            java.util.List<Object> list = new java.util.ArrayList<>(len);
            for (int i = 0; i < len; i++) {
                list.add(java.lang.reflect.Array.get(val, i));
            }
            return list;
        }
        throw new CommonException("无法转换为集合: " + val.getClass());
    }
}
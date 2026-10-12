package com.guilu.utils.queryUtil;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;

public class DefaultCreateUtils<T>{
    //准确数值型的eq查询类型
    private static final Set<Class<?>> EQ_TYPES = Set.of(
            Integer.class, int.class,
            Long.class, long.class,
            Short.class, short.class,
            Byte.class, byte.class,
            Double.class, double.class,
            Float.class, float.class,
            Boolean.class, boolean.class,
            java.math.BigDecimal.class,
            java.math.BigInteger.class
    );
    //like的默认查询类型
    private static final Set<Class<?>> LIKE_TYPES = Set.of(
            String.class,
            Character.class, char.class,StringBuilder.class,StringBuffer.class
    );

    //ge类型的查询类型
    private static final Set<Class<?>> DATE_TYPES = Set.of(
            java.time.LocalDate.class,
            java.time.LocalDateTime.class,
            java.time.LocalTime.class,
            java.util.Date.class,
            java.sql.Timestamp.class
    );
    //默认Wrapper生成器
    public QueryWrapper<T> generateDefaultValueWrapper(Object tar) {
        QueryWrapper<T> qw = new QueryWrapper<>();
        if (tar == null) {
            return qw;
        }

        for (Field field : getAllFields(tar.getClass())) {
            // 跳过静态字段、合成字段
            if (Modifier.isStatic(field.getModifiers()) || field.isSynthetic()) {
                continue;
            }
            if ("serialVersionUID".equals(field.getName())) {
                continue;
            }

            field.setAccessible(true);
            Object value;
            try {
                value = field.get(tar);
            } catch (IllegalAccessException e) {
                continue;
            }
            if (isEmpty(value)) {
                continue;
            }

            String colName = toFiled(field.getName());
            Class<?> fieldType = field.getType();
            applyCondition(qw, colName, fieldType, value);
        }

        return qw;
    }

    private void applyCondition(QueryWrapper<T> qw, String col, Class<?> type, Object value) {
        if (value instanceof Collection<?> coll) {
            if (!coll.isEmpty()) {
                qw.in(col, coll);
            }
            return;
        }
        if (value.getClass().isArray()) {
            qw.in(col, toList(value));
            return;
        }
        if (LIKE_TYPES.contains(type)) {
            qw.like(col, value);
            return;
        }
        if (EQ_TYPES.contains(type) || type.isEnum()) {
            qw.eq(col, value);
            return;
        }
        //时间类型详细的查询
        if (DATE_TYPES.contains(type)) {
            qw.ge(col, value);
            return;
        }
        qw.eq(col, value);
    }

    /**
     * 判断值是否为空
     */
    private boolean isEmpty(Object value) {
        if (value == null) {
            return true;
        }
        if (value instanceof CharSequence cs) {
            return cs.toString().trim().isEmpty();
        }
        if (value instanceof Collection<?> coll) {
            return coll.isEmpty();
        }
        if (value.getClass().isArray()) {
            return Array.getLength(value) == 0;
        }
        return false;
    }

    private List<Object> toList(Object array) {
        return getObjects(array);
    }

    static List<Object> getObjects(Object array) {
        int len = Array.getLength(array);
        List<Object> list = new java.util.ArrayList<>(len);
        for (int i = 0; i < len; i++) {
            list.add(Array.get(array, i));
        }
        return list;
    }

    private List<Field> getAllFields(Class<?> clazz) {
        List<Field> fields = new ArrayList<>();
        Collections.addAll(fields,clazz.getDeclaredFields());
        return fields;
    }
    protected String toFiled(String name) {
        if (name == null || name.isEmpty()) return name;

        StringBuilder sb = new StringBuilder(name.length() + 8);
        char[] chars = name.toCharArray();

        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];
            if (Character.isUpperCase(c)) {
                //前一个是小写/数字 -> 直接插
                //前一个是大写、后一个是小写
                boolean prevExists   = i > 0;
                boolean prevNotUpper = prevExists && !Character.isUpperCase(chars[i - 1]);
                boolean nextIsLower  = (i + 1 < chars.length) && Character.isLowerCase(chars[i + 1]);
                boolean prevIsUpper  = prevExists && Character.isUpperCase(chars[i - 1]);

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
}

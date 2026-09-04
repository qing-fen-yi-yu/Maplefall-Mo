package com.guilu.validate.annotations;

import com.guilu.validate.EnumValidator;
import com.guilu.validate.EnumValueValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * 用于状态的枚举校验
 **/
@Documented
@Retention(RetentionPolicy.RUNTIME) //仅在运行时生效
@Target({ ElementType.PARAMETER, ElementType.FIELD, ElementType.METHOD }) //定义在参数、方法、字段
@Constraint(validatedBy = {EnumValidator.class, EnumValueValidator.class}) //交给EnumValueValidator和EnumValidator进行处理
public @interface EnumValid {
    String message() default "不满足业务条件";

    int[] enumeration() default {};

    Class<?>[] groups() default { };

    Class<? extends Payload>[] payload() default { };
}

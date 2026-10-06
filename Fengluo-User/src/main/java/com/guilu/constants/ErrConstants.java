package com.guilu.constants;

public interface ErrConstants {
    interface ACCOUNT{
        /** 注册冲突提示，经 BadRequestException 映射为 400 */
        String USERNAME_ALREADY_EXISTS = "用户名已存在";
        String EMAIL_ALREADY_EXISTS = "邮箱已被注册";
        String REGISTER_CONFLICT = "注册信息已存在，请更换用户名或邮箱";
    }
}

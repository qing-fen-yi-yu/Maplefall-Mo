package com.guilu.constants;

public interface ErrConstants {
    interface ACCOUNT{
        /** 注册冲突提示，经 BadRequestException 映射为 400 */
        String USERNAME_ALREADY_EXISTS = "用户名已存在";
        String EMAIL_ALREADY_EXISTS = "邮箱已被注册";
        String REGISTER_CONFLICT = "注册信息已存在，请更换用户名或邮箱";
    }

    interface COMMON {
        String ID_REQUIRED = "主键ID禁止为空";
        /** 用户与角色的 status 列语义一致：0禁用 / 1正常 */
        String INVALID_STATUS = "状态值不合法，只允许 0禁用 / 1正常";
    }

    interface USER {
        String USERNAME_REQUIRED = "用户名禁止为空";
        String PASSWORD_REQUIRED = "密码禁止为空";
        String OLD_PASSWORD_REQUIRED = "原密码禁止为空";
        String NEW_PASSWORD_REQUIRED = "新密码禁止为空";
        String OLD_PASSWORD_WRONG = "原密码错误";
        String PASSWORD_UNCHANGED = "新密码不能与原密码相同";
        String SELF_STATUS_FORBIDDEN = "不允许修改当前登录账号的状态";
        String SELF_DELETE_FORBIDDEN = "不允许删除当前登录账号";
    }

    interface ROLE {
        String ROLE_CODE_REQUIRED = "角色编码禁止为空";
        String ROLE_NAME_REQUIRED = "角色名称禁止为空";
        String ROLE_CODE_ALREADY_EXISTS = "角色编码已存在";
        String ROLE_NOT_EXISTS = "角色不存在";
        String ROLE_DISABLED = "角色已停用，无法分配";
        String ROLE_IN_USE = "角色已被用户绑定，无法删除";
        String DEFAULT_ROLE_FORBIDDEN = "默认角色不允许删除";
        String DEFAULT_ROLE_DISABLE_FORBIDDEN = "默认角色不允许停用，否则新用户注册与角色降级都会失效";
        String DEFAULT_ROLE_MAPPING_FORBIDDEN = "默认角色映射不允许删除，如需降级请调用降级接口";
        String MENU_NOT_EXISTS = "存在无效的菜单/权限ID，请刷新权限树后重试";
        String PRIVILEGE_PUBLISH_FAILED = "权限配置已保存，但权限缓存刷新失败，请调用 /role/privilege/republish 重试";
    }
}

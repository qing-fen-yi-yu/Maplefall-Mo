package com.guilu.domain.po;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 系统用户
 * </p>
 *
 * @author 归鹭
 * @since 2026-09-25
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("sys_user")
public class SysUser implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 登录用户名，系统内唯一
     */
    private String username;

    /**
     * 用户显示名称
     */
    private String nickname;

    /**
     * 密码摘要，禁止保存明文密码
     */
    @TableField("password_hash")
    private String password;

    /**
     * 用户邮箱
     */
    private String email;

    /**
     * 用户手机号
     */
    private String phone;

    /**
     * 用户头像地址
     */
    private String avatarUrl;

    /**
     * 用户状态：0禁用，1正常
     */
    private Integer status;

    /**
     * 是否超级管理员：0否，1是
     */
    private Integer superAdmin;

    /**
     * 最近一次登录时间
     */
    private LocalDateTime lastLoginAt;

    /**
     * 最近一次登录IP地址
     */
    private String lastLoginIp;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 最后修改时间
     */
    private LocalDateTime updatedAt;

    /**
     * 0未删除，1已删除
     */
    private Integer deleted;
}

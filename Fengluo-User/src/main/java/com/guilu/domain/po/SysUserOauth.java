package com.guilu.domain.po;

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
 * 第三方登录绑定
 * </p>
 *
 * @author 归鹭
 * @since 2026-09-25
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("sys_user_oauth")
public class SysUserOauth implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 系统用户ID
     */
    private Long userId;

    /**
     * 登录平台类型
     */
    private String provider;

    /**
     * 第三方平台用户ID
     */
    private String providerUserId;

    /**
     * 第三方平台统一用户标识
     */
    private String unionId;

    /**
     * 第三方平台OpenID
     */
    private String openId;

    /**
     * 第三方平台用户昵称
     */
    private String nickname;

    /**
     * 第三方平台用户头像地址
     */
    private String avatarUrl;

    /**
     * 加密保存的AccessToken
     */
    private String accessTokenCipher;

    /**
     * 加密保存的RefreshToken
     */
    private String refreshTokenCipher;

    /**
     * Token过期时间
     */
    private LocalDateTime tokenExpiresAt;

    /**
     * 第三方授权范围
     */
    private String scope;

    /**
     * 第三方平台扩展信息
     */
    private String extraJson;

    /**
     * 最近一次通过该第三方账号登录时间
     */
    private LocalDateTime lastLoginAt;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 最后修改时间
     */
    private LocalDateTime updatedAt;


}

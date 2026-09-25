-- ============================================================
-- fl_auth 联调种子数据
--
-- 目的：让双 token 登录 + 网关 RBAC 拦截可以被端到端验证。
-- 口令均为明文写入下方注释，password_hash 由 hutool BCrypt 生成：
--   admin    / admin123
--   operator / operator123
--
-- 权限表达式规则（必须遵守，网关按 {METHOD}:{path} 做 Ant 匹配）：
--   *:/user/sys-role/**  -> 任意方法，匹配 /user/sys-role 及其子路径
--   GET:/user/x/list     -> 仅 GET
--   /user/x/**           -> 错误！缺少方法前缀，永远匹配不上，该路径等于不设防
--
-- 预期结果矩阵（网关侧）：
--   调用者       /user/sys-user/list   /user/sys-role/list   /user/sys-menu/list
--   无 token     401                   401                   401
--   operator     放行(下游404)         403                   403
--   admin        放行(下游404)         放行(下游404)         放行(下游404)
--   （放行后因生成的 Controller 尚无处理方法而返回 404，说明已通过网关鉴权）
-- ============================================================

USE `fl_auth`;

INSERT INTO `sys_role` (`id`, `role_code`, `role_name`, `description`, `status`, `sort`, `deleted`, `version`)
VALUES (1, 'admin', '超级管理员', '拥有全部权限', 1, 1, 0, 0),
       (2, 'operator', '普通运营', '仅可访问用户管理', 1, 2, 0, 0);

INSERT INTO `sys_user` (`id`, `username`, `nickname`, `password_hash`, `status`, `super_admin`, `deleted`)
VALUES (1, 'admin', '管理员',
        '$2a$10$5B9xvrOV1JvnLiO4vWM6Cu/wa9AEUuvO87EoGtgC5zL6SShzwmRaq', 1, 1, 0),
       (2, 'operator', '运营',
        '$2a$10$/cumW1CpRTw2GlypLGBya.zQCzznEJEdBeKXbLfgEE1c.w3fkpZTS', 1, 0, 0);

-- 注意 permission 用的是后端接口路径，不是 sys_menu.path（前端路由）
INSERT INTO `sys_menu` (`id`, `parent_id`, `type`, `menu_name`, `permission`, `sort`, `visible`, `status`, `deleted`)
VALUES (1, 0, '菜单', '用户管理', '*:/user/sys-user/**', 1, 1, 1, 0),
       (2, 0, '菜单', '角色管理', '*:/user/sys-role/**', 2, 1, 1, 0),
       (3, 0, '菜单', '菜单管理', '*:/user/sys-menu/**', 3, 1, 1, 0);

INSERT INTO `sys_user_role` (`user_id`, `role_id`)
VALUES (1, 1),
       (2, 2);

INSERT INTO `sys_role_menu` (`role_id`, `menu_id`)
VALUES (1, 1), (1, 2), (1, 3),   -- admin 可访问三个菜单
       (2, 1);                  -- operator 仅可访问用户管理

CREATE TABLE `user`
(
    `id`            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_code`     VARCHAR(36)  NOT NULL COMMENT '业务主键',
    `username`      VARCHAR(32)  NOT NULL COMMENT '用户名',
    `password`      VARCHAR(255) NOT NULL COMMENT '密码',
    `nick_name`     VARCHAR(32) COMMENT '用户昵称',
    `last_login_at` DATETIME COMMENT '最后登录时间',
    `current_token` VARCHAR(255) COMMENT '当前登录token',
    `created_at`    DATETIME     NOT NULL COMMENT '创建时间',
    `updated_at`    DATETIME     NOT NULL COMMENT '更新时间',
    UNIQUE KEY `uk_user_code` (`user_code`),
    UNIQUE KEY `uk_username` (`username`),
    UNIQUE KEY `uk_current_token` (`current_token`),
    PRIMARY KEY (`id`)
) COMMENT '用户';

# 初始化管理员 admin / admin
INSERT INTO user (user_code, username, password, nick_name, last_login_at, current_token, created_at,
                                 updated_at)
VALUES ('5291aa8f925441a093cd197c3cc6d3c5', 'admin', '$2a$10$lcN2wzbdX5gC.nBtrr9/8u/ZGhpPs7S58po.KA6CR/vq3TG.1lGvy',
        'Admin', NULL, NULL, '2026-06-08 12:46:58', '2026-06-08 12:47:00');

CREATE TABLE `role`
(
    `id`          BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键',
    `role_code`   VARCHAR(36) NOT NULL COMMENT '业务主键',
    `role_name`   VARCHAR(32) NOT NULL COMMENT '角色名称',
    `description` VARCHAR(128) COMMENT '角色描述',
    `created_at`  DATETIME    NOT NULL COMMENT '创建时间',
    `updated_at`  DATETIME    NOT NULL COMMENT '更新时间',
    UNIQUE KEY `uk_role_code` (`role_code`),
    UNIQUE KEY `uk_role_name` (`role_name`),
    PRIMARY KEY (`id`)
) COMMENT '角色';

CREATE TABLE `role_permission` (
    `id`              BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键',
    `role_id`         BIGINT      NOT NULL COMMENT '角色ID',
    `permission_code` VARCHAR(64) NOT NULL COMMENT '权限编码',
    `created_at`      DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY `uk_role_permission` (`role_id`, `permission_code`),
    PRIMARY KEY (`id`)
) COMMENT '角色-权限关联';

CREATE TABLE `user_role` (
    `id`         BIGINT   NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id`    BIGINT   NOT NULL COMMENT '用户ID',
    `role_id`    BIGINT   NOT NULL COMMENT '角色ID',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY `uk_user_role` (`user_id`, `role_id`),
    PRIMARY KEY (`id`)
) COMMENT '用户-角色关联';


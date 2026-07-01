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
    `created_by`    VARCHAR(32) COMMENT '创建人',
    `updated_at`    DATETIME     NOT NULL COMMENT '更新时间',
    `updated_by`    VARCHAR(32) COMMENT '更新人',
    UNIQUE KEY `uk_user_code` (`user_code`),
    UNIQUE KEY `uk_username` (`username`),
    UNIQUE KEY `uk_current_token` (`current_token`),
    PRIMARY KEY (`id`)
) COMMENT '用户';

CREATE TABLE `role`
(
    `id`          BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键',
    `role_code`   VARCHAR(36) NOT NULL COMMENT '业务主键',
    `role_name`   VARCHAR(32) NOT NULL COMMENT '角色名称',
    `description` VARCHAR(128) COMMENT '角色描述',
    `created_at`  DATETIME    NOT NULL COMMENT '创建时间',
    `created_by`  VARCHAR(32) COMMENT '创建人',
    `updated_at`  DATETIME    NOT NULL COMMENT '更新时间',
    `updated_by`  VARCHAR(32) COMMENT '更新人',
    UNIQUE KEY `uk_role_code` (`role_code`),
    UNIQUE KEY `uk_role_name` (`role_name`),
    PRIMARY KEY (`id`)
) COMMENT '角色';

CREATE TABLE `role_permission` (
    `id`              BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键',
    `role_id`         BIGINT      NOT NULL COMMENT '角色ID',
    `permission_code` VARCHAR(64) NOT NULL COMMENT '权限编码',
    `created_at`      DATETIME    NOT NULL COMMENT '创建时间',
    `created_by`      VARCHAR(32) COMMENT '创建人',
    UNIQUE KEY `uk_role_permission` (`role_id`, `permission_code`),
    PRIMARY KEY (`id`)
) COMMENT '角色-权限关联';

CREATE TABLE `user_role` (
    `id`         BIGINT   NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id`    BIGINT   NOT NULL COMMENT '用户ID',
    `role_id`    BIGINT   NOT NULL COMMENT '角色ID',
    `created_at` DATETIME NOT NULL COMMENT '创建时间',
    `created_by` VARCHAR(32) COMMENT '创建人',
    UNIQUE KEY `uk_user_role` (`user_id`, `role_id`),
    PRIMARY KEY (`id`)
) COMMENT '用户-角色关联';

CREATE TABLE `audit_log`
(
    `id`             BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键',
    `audit_log_code` VARCHAR(36) NOT NULL COMMENT '业务主键',
    `operation_type` VARCHAR(64) NOT NULL COMMENT '操作类型',
    `success`        TINYINT(1)  NOT NULL COMMENT '是否成功',
    `content`        LONGTEXT COMMENT '操作内容',
    `fail_reason`    VARCHAR(512) COMMENT '失败原因',
    `created_at`     DATETIME    NOT NULL COMMENT '创建时间',
    `created_by`     VARCHAR(32) NOT NULL COMMENT '创建人'
    UNIQUE KEY `uk_audit_log_code` (`audit_log_code`),
    PRIMARY KEY (`id`)
) COMMENT '审计日志';

CREATE TABLE `file_record`
(
    `id`               BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `file_key`         VARCHAR(255) NOT NULL COMMENT '文件Key（uuid+扩展名），业务唯一键，业务表引用此值',
    `origin_file_name` VARCHAR(255) NOT NULL COMMENT '上传时的原始文件名',
    `content_type`     VARCHAR(128) NOT NULL COMMENT 'MIME类型，下载/预览时回填Content-Type',
    `file_size`        BIGINT       NOT NULL COMMENT '文件大小（字节）',
    `category`         VARCHAR(32)  NOT NULL COMMENT '上传时的文件类别（image/document/general等）',
    `bucket`           VARCHAR(64) COMMENT '存储桶名称，本地存储时为空',
    `created_at`       DATETIME     NOT NULL COMMENT '创建时间',
    `created_by`       VARCHAR(32) COMMENT '创建人',
    UNIQUE KEY `uk_file_key` (`file_key`),
    PRIMARY KEY (`id`)
) COMMENT '文件记录';
package com.example.roletest.entity;

import java.time.LocalDateTime;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

/**
 * 角色
 * <p>role
 * <p>
 * <p>Any modifications may be overwritten by future code generations.
 *
 * @author Deolin 2026-06-21
 */
@Data
@Accessors(chain = true)
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class RoleEntity {

    /**
     * 主键
     * <p>id
     * <p>不能为null
     */
    Long id;

    /**
     * 业务主键
     * <p>role_code
     * <p>长度：36
     * <p>不能为null
     */
    String roleCode;

    /**
     * 角色名称
     * <p>role_name
     * <p>长度：32
     * <p>不能为null
     */
    String roleName;

    /**
     * 角色描述
     * <p>description
     * <p>长度：128
     */
    String description;

    /**
     * 创建时间
     * <p>created_at
     * <p>不能为null
     */
    LocalDateTime createdAt;

    /**
     * 更新时间
     * <p>updated_at
     * <p>不能为null
     */
    LocalDateTime updatedAt;
}

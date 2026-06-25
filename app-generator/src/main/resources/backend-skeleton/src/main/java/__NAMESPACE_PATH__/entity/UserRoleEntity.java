package __NAMESPACE__.entity;

import java.time.LocalDateTime;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

/**
 * 角色-权限关联
 * <p>role_permission
 * <p>
 * <p>Any modifications may be overwritten by future code generations.
 *
 * @author Deolin 2026-06-21
 */
@Data
@Accessors(chain = true)
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class UserRoleEntity {

    /**
     * 主键
     * <p>id
     * <p>不能为null
     */
    Long id;

    /**
     * 用户ID
     * <p>user_id
     * <p>不能为null
     */
    Long userId;

    /**
     * 角色ID
     * <p>role_id
     * <p>不能为null
     */
    Long roleId;

    /**
     * 创建时间
     * <p>created_at
     * <p>不能为null
     * <p>默认：CURRENT_TIMESTAMP
     */
    LocalDateTime createdAt;

    /**
     * 创建人
     * <p>created_by
     * <p>长度：32
     */
    String createdBy;
}

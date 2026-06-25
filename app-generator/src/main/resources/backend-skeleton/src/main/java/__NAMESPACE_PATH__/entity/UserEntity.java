package __NAMESPACE__.entity;

import java.time.LocalDateTime;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

/**
 * 用户
 * <p>user
 * <p>
 * <p>Any modifications may be overwritten by future code generations.
 *
 * @author Deolin 2026-06-07
 */
@Data
@Accessors(chain = true)
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class UserEntity {

    /**
     * 主键
     * <p>id
     * <p>不能为null
     */
    Long id;

    /**
     * 业务主键
     * <p>user_code
     * <p>长度：36
     * <p>不能为null
     */
    String userCode;

    /**
     * 用户名
     * <p>username
     * <p>长度：32
     * <p>不能为null
     */
    String username;

    /**
     * 密码
     * <p>password
     * <p>长度：255
     * <p>不能为null
     */
    String password;

    /**
     * 用户昵称
     * <p>nick_name
     * <p>长度：32
     */
    String nickName;

    /**
     * 最后登录时间
     * <p>last_login_at
     */
    LocalDateTime lastLoginAt;

    /**
     * 当前登录token
     * <p>current_token
     * <p>长度：255
     */
    String currentToken;

    /**
     * 创建时间
     * <p>created_at
     * <p>不能为null
     */
    LocalDateTime createdAt;

    /**
     * 创建人
     * <p>created_by
     * <p>长度：32
     */
    String createdBy;

    /**
     * 更新时间
     * <p>updated_at
     * <p>不能为null
     */
    LocalDateTime updatedAt;

    /**
     * 更新人
     * <p>updated_by
     * <p>长度：32
     */
    String updatedBy;

}

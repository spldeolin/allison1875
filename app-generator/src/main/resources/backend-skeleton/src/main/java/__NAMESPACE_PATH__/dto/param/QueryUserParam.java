package __NAMESPACE__.dto.param;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

/**
 * @author Deolin 2026-06-07
 */
@Data
@Accessors(chain = true)
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class QueryUserParam {

    /**
     * 业务主键
     */
    List<String> userCode;

    /**
     * 用户名
     */
    String username;

    /**
     * 用户昵称
     */
    String nickName;

    /**
     * 最后登录时间
     */
    LocalDateTime lastLoginAt;

    /**
     * 最后登录时间
     */
    LocalDateTime lastLoginAtEx;

    /**
     * 创建时间
     */
    LocalDateTime createdAt;

    /**
     * 创建时间
     */
    LocalDateTime createdAtEx;

    /**
     *
     */
    Integer offset;

    /**
     *
     */
    Integer limit;

}

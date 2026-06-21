package __NAMESPACE__.dto.resp;

import java.time.LocalDateTime;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

/**
 * @author Deolin 2026-06-07
 */
@Data
@Accessors(chain = true)
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class ListUsersResp {

    /**
     * 业务主键
     */
    String userCode;

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
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    LocalDateTime lastLoginAt;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    LocalDateTime updatedAt;

    /**
     * 已授予角色
     */
    List<RoleBriefResp> grantedRoles;

    /**
     * 已授予权限（取并集后的 permission code 列表）
     */
    List<String> grantedPermissions;

}

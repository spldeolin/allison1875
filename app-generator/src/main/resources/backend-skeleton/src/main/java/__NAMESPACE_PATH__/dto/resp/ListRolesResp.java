package __NAMESPACE__.dto.resp;

import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

/**
 * @author Deolin 2026-06-21
 */
@Data
@Accessors(chain = true)
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class ListRolesResp {

    /**
     * 业务主键
     */
    String roleCode;

    /**
     * 角色名称
     */
    String roleName;

    /**
     * 角色描述
     */
    String description;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    LocalDateTime createdAt;

    /**
     * 创建人
     */
    String createdBy;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    LocalDateTime updatedAt;

    /**
     * 最近更新人
     */
    String updatedBy;
}

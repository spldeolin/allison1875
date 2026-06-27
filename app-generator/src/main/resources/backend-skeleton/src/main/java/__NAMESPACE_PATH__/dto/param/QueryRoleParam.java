package __NAMESPACE__.dto.param;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

/**
 * @author Deolin 2026-06-27
 */
@Data
@Accessors(chain = true)
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class QueryRoleParam {

    /**
     * 业务主键
     */
    List<String> roleCode;

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
    LocalDateTime createdAt;

    /**
     * 创建时间
     */
    LocalDateTime createdAtEx;

    /**
     */
    Integer offset;

    /**
     */
    Integer limit;

    /**
     * 排序字段
     */
    String sortBy;

    /**
     * 是否正序
     */
    Boolean isAsc;
}

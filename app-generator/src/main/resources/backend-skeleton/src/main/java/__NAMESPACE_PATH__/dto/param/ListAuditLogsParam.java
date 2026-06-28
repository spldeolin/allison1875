package __NAMESPACE__.dto.param;

import java.util.List;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

/**
 * @author Deolin 2026-06-27
 */
@Data
@Accessors(chain = true)
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class ListAuditLogsParam {

    /**
     * 业务主键
     */
    List<String> auditLogCode;

    /**
     * 操作类型
     */
    List<String> operationType;

    /**
     * 是否成功
     */
    List<Boolean> success;

    /**
     * 操作内容
     */
    String content;

    /**
     * 失败原因
     */
    String failReason;

    /**
     * 创建时间
     */
    LocalDateTime createdAt;

    /**
     * 创建时间
     */
    LocalDateTime createdAtEx;

    /**
     * 创建人
     */
    String createdBy;

    /**
     * 最近更新人
     */
    String updatedBy;

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

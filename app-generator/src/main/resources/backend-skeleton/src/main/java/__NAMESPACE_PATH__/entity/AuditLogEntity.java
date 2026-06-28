package __NAMESPACE__.entity;

import java.time.LocalDateTime;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

/**
 * 审计日志
 * <p>audit_log
 * <p>
 * <p>Any modifications may be overwritten by future code generations.
 *
 * @author Deolin 2026-06-27
 */
@Data
@Accessors(chain = true)
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class AuditLogEntity {

    /**
     * 主键
     * <p>id
     * <p>不能为null
     */
    Long id;

    /**
     * 业务主键
     * <p>audit_log_code
     * <p>长度：36
     * <p>不能为null
     */
    String auditLogCode;

    /**
     * 操作类型
     * <p>operation_type
     * <p>长度：64
     * <p>不能为null
     */
    String operationType;

    /**
     * 是否成功
     * <p>success
     * <p>长度：1
     * <p>不能为null
     */
    Boolean success;

    /**
     * 操作内容
     * <p>content
     */
    String content;

    /**
     * 失败原因
     * <p>fail_reason
     * <p>长度：512
     */
    String failReason;

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
     * <p>不能为null
     */
    String createdBy;

}

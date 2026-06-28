package __NAMESPACE__.dto.resp;

import __NAMESPACE__.entity.*;
import __NAMESPACE__.enums.*;
import java.util.*;
import java.time.*;
import java.math.*;
import com.fasterxml.jackson.annotation.*;
import java.util.stream.*;
import org.springframework.util.*;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

/**
 * @author Deolin 2026-06-27
 */
@Data
@Accessors(chain = true)
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class ListAuditLogsResp {

    /**
     * 业务主键
     */
    String auditLogCode;

    /**
     * 操作类型
     */
    AuditOperationTypeEnum operationType;

    /**
     * 是否成功
     */
    Boolean success;

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
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    LocalDateTime updatedAt;

    /**
     * 创建人
     */
    String createdBy;

    /**
     * 最近更新人
     */
    String updatedBy;
}

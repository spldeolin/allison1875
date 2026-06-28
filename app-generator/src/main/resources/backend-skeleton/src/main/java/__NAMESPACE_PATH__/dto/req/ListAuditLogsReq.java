package __NAMESPACE__.dto.req;

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
public class ListAuditLogsReq {

    /**
     * 按业务主键列表过滤，null或empty代表无需过滤
     */
    List<String> auditLogCode;

    /**
     * 按“操作类型”列表过滤，null或empty代表无需过滤
     */
    List<AuditOperationTypeEnum> operationType;

    /**
     * 按“是否成功”列表过滤，null或empty代表无需过滤
     */
    List<Boolean> success;

    /**
     * 按“操作内容”模糊匹配过滤，null或empty代表无需过滤
     */
    String content;

    /**
     * 按“失败原因”模糊匹配过滤，null或empty代表无需过滤
     */
    String failReason;

    /**
     * 创建人
     */
    String createdBy;

    /**
     * 最近更新人
     */
    String updatedBy;

    /**
     * 按创建时间晚于该时间过滤，null代表无需过滤
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    LocalDateTime createdAtStart;

    /**
     * 按创建时间早于该时间过滤，null代表无需过滤
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    LocalDateTime createdAtEnd;

    /**
     * 分页页码
     */
    Integer pageNum = 1;

    /**
     * 分页条数
     */
    Integer pageSize = 10;

    /**
     * 排序字段，null代表更新时间倒序
     */
    AuditLogSortEnum sortBy;

    /**
     * true代表正序，否则代表倒序
     */
    Boolean isAsc;
}

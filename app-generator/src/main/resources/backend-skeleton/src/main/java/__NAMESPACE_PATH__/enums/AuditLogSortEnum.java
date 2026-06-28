package __NAMESPACE__.enums;

import lombok.Getter;
import lombok.AllArgsConstructor;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.Arrays;

/**
 * 审计日志的排序字段
 *
 * @author Deolin 2026-06-27
 */
@Getter
@AllArgsConstructor
public enum AuditLogSortEnum {

    AUDIT_LOG_CODE("auditLogCode", "按“业务主键”排序"),
    SUCCESS("success", "按“是否成功”排序"),
    CONTENT("content", "按“操作内容”排序"),
    FAIL_REASON("failReason", "按“失败原因”排序"),
    CREATED_AT("createdAt", "按“创建时间”排序"),
    UPDATED_AT("updatedAt", "按“更新时间”排序"),
    CREATED_BY("createdBy", "按“创建人”排序"),
    UPDATED_BY("updatedBy", "按“最近更新人”排序");

    @JsonValue
    private final String code;

    private final String title;

    /**
     * 判断参数code是否是一个有效的枚举
     */
    public static boolean valid(String code) {
        return Arrays.stream(values()).anyMatch(anEnum -> anEnum.getCode().equals(code));
    }

    /**
     * 获取code对应的枚举
     */
    @JsonCreator
    public static AuditLogSortEnum of(String code) {
        return Arrays.stream(values()).filter(anEnum -> anEnum.getCode().equals(code)).findFirst().orElse(null);
    }

    @Override
    public String toString() {
        return code;
    }
}

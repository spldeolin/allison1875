package __NAMESPACE__.service;

import java.util.Map;
import __NAMESPACE__.enums.AuditOperationTypeEnum;

/**
 * @author Deolin 2026-06-27
 */
public interface AuditLogFacade {

    void logSuccess(AuditOperationTypeEnum operationType, Map<String, Object> content);

    void logFailure(AuditOperationTypeEnum operationType, String failReason);

    void logUpdateSuccess(AuditOperationTypeEnum operationType,
            Map<String, Object> oldValues, Map<String, Object> newValues);

    void logUpdateFailure(AuditOperationTypeEnum operationType, String failReason);

    /**
     * 显式指定操作人的审计日志写入。
     * <p>用于登录/退出登录等场景：此时登录态可能尚未建立或已失效，
     * 无法从 {@link __NAMESPACE__.common.CurrentUser} 获取操作人，
     * 因此由调用方显式传入登录期间的用户名。
     *
     * @param operator 操作人用户名
     */
    void logSuccess(AuditOperationTypeEnum operationType, Map<String, Object> content, String operator);

    void logFailure(AuditOperationTypeEnum operationType, String failReason, String operator);

}

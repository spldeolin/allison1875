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

}

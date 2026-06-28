package __NAMESPACE__.service.impl;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import javax.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import __NAMESPACE__.common.CurrentUser;
import __NAMESPACE__.entity.AuditLogEntity;
import __NAMESPACE__.enums.AuditOperationTypeEnum;
import __NAMESPACE__.mapper.AuditLogMapper;
import __NAMESPACE__.service.AuditLogFacade;
import __NAMESPACE__.util.JsonUtils;
import __NAMESPACE__.util.UuidUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Deolin 2026-06-27
 */
@Slf4j
@Service
public class AuditLogFacadeImpl implements AuditLogFacade {

    @Resource
    private AuditLogMapper auditLogMapper;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public void logSuccess(AuditOperationTypeEnum operationType, Map<String, Object> content) {
        insertAuditLog(operationType, true, serializeContent(content), null);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public void logFailure(AuditOperationTypeEnum operationType, String failReason) {
        insertAuditLog(operationType, false, null, failReason);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public void logUpdateSuccess(AuditOperationTypeEnum operationType,
            Map<String, Object> oldValues, Map<String, Object> newValues) {
        insertAuditLog(operationType, true, diffAndSerialize(oldValues, newValues), null);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public void logUpdateFailure(AuditOperationTypeEnum operationType, String failReason) {
        insertAuditLog(operationType, false, null, failReason);
    }

    private void insertAuditLog(AuditOperationTypeEnum operationType, boolean success,
            String content, String failReason) {
        AuditLogEntity entity = new AuditLogEntity();
        entity.setAuditLogCode(UuidUtils.generateShort());
        entity.setOperationType(operationType.getCode());
        entity.setSuccess(success);
        entity.setContent(content);
        entity.setFailReason(failReason);
        entity.setCreatedAt(LocalDateTime.now());
        entity.setCreatedBy(CurrentUser.getUsernameOrDefault("system"));
        auditLogMapper.insert(entity);
    }

    private String serializeContent(Map<String, Object> content) {
        if (content == null || content.isEmpty()) {
            return null;
        }
        return JsonUtils.toJson(content);
    }

    private String diffAndSerialize(Map<String, Object> oldValues, Map<String, Object> newValues) {
        if (oldValues == null || newValues == null) {
            return null;
        }
        Map<String, Object> changes = new LinkedHashMap<>();
        for (Map.Entry<String, Object> entry : oldValues.entrySet()) {
            String key = entry.getKey();
            Object oldVal = entry.getValue();
            Object newVal = newValues.get(key);
            if (!Objects.equals(oldVal, newVal)) {
                Map<String, Object> change = new LinkedHashMap<>();
                change.put("old", oldVal);
                change.put("new", newVal);
                changes.put(key, change);
            }
        }
        if (changes.isEmpty()) {
            return "无变更内容";
        }
        return JsonUtils.toJson(changes);
    }

}

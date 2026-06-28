package __NAMESPACE__.service.impl;

import __NAMESPACE__.entity.*;
import __NAMESPACE__.enums.*;
import com.fasterxml.jackson.annotation.*;
import java.util.stream.*;
import org.springframework.util.*;
import __NAMESPACE__.service.AuditLogService;
import __NAMESPACE__.mapper.AuditLogMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import __NAMESPACE__.dto.req.ListAuditLogsReq;
import __NAMESPACE__.dto.resp.PageResult;
import __NAMESPACE__.dto.resp.ListAuditLogsResp;
import java.time.*;
import java.math.*;
import java.util.*;
import __NAMESPACE__.dto.param.ListAuditLogsParam;

/**
 * @author Deolin
 */
@Slf4j
@Service
public class AuditLogServiceImpl implements AuditLogService {

    @Resource
    private AuditLogMapper auditLogMapper;

    private final Long listAuditLogsTotal = 0L;

    @Override
    public PageResult<ListAuditLogsResp> listAuditLogs(ListAuditLogsReq req) {
    final ListAuditLogsParam listAuditLogsParam = new ListAuditLogsParam();
    listAuditLogsParam.setAuditLogCode(req.getAuditLogCode());
    listAuditLogsParam.setOperationType(req.getOperationType() == null ? null : req.getOperationType().stream().map(AuditOperationTypeEnum::getCode).collect(Collectors.toList()));
    listAuditLogsParam.setSuccess(req.getSuccess() == null || req.getSuccess().isEmpty() ? null : req.getSuccess());
    listAuditLogsParam.setContent(req.getContent());
    listAuditLogsParam.setFailReason(req.getFailReason());
    listAuditLogsParam.setCreatedAt(req.getCreatedAtStart() == null ? null : req.getCreatedAtStart());
    listAuditLogsParam.setCreatedAtEx(req.getCreatedAtEnd() == null ? null : req.getCreatedAtEnd());
    listAuditLogsParam.setCreatedBy(req.getCreatedBy());
    listAuditLogsParam.setOffset((req.getPageNum() - 1) * req.getPageSize());
    listAuditLogsParam.setLimit(req.getPageSize());
    listAuditLogsParam.setSortBy(req.getSortBy() != null ? req.getSortBy().getCode() : null);
    listAuditLogsParam.setIsAsc(req.getIsAsc());
    long listAuditLogsTotal = auditLogMapper.countListAuditLogs(listAuditLogsParam);
    List<AuditLogEntity> auditLogs = auditLogMapper.listAuditLogs(listAuditLogsParam);
        if (auditLogs.isEmpty()) {
            return PageResult.empty();
        }
        List<ListAuditLogsResp> dtos = new ArrayList<>();
        for (AuditLogEntity auditLog : auditLogs) {
            ListAuditLogsResp dto = new ListAuditLogsResp();
            dto.setAuditLogCode(auditLog.getAuditLogCode());
            dto.setOperationType(AuditOperationTypeEnum.of(auditLog.getOperationType()));
            dto.setSuccess(auditLog.getSuccess());
            dto.setContent(auditLog.getContent());
            dto.setFailReason(auditLog.getFailReason());
            dto.setCreatedAt(auditLog.getCreatedAt());
            dto.setCreatedBy(auditLog.getCreatedBy());
            dtos.add(dto);
        }
        return PageResult.of(listAuditLogsTotal, dtos);
    }

}

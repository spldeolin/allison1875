package __NAMESPACE__.controller;

import __NAMESPACE__.entity.*;
import __NAMESPACE__.enums.*;
import java.util.*;
import java.time.*;
import java.math.*;
import com.fasterxml.jackson.annotation.*;
import java.util.stream.*;
import org.springframework.util.*;
import __NAMESPACE__.service.AuditLogService;
import __NAMESPACE__.common.RequestResult;
import __NAMESPACE__.dto.resp.PageResult;
import __NAMESPACE__.dto.resp.ListAuditLogsResp;
import __NAMESPACE__.dto.req.ListAuditLogsReq;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import javax.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import javax.validation.Valid;
import __NAMESPACE__.annotation.WebApiAuth;
import __NAMESPACE__.enums.PermissionEnum;

/**
 * 审计日志
 *
 * @author Deolin
 */
@RestController
@RequestMapping("/api/v1/auditLog")
public class AuditLogController {

    @Resource
    private AuditLogService auditLogService;

    /**
     * 审计日志列表
     */
    @WebApiAuth(PermissionEnum.LIST_AUDIT_LOG)
    @PostMapping("listAuditLogs")
    public RequestResult<PageResult<ListAuditLogsResp>> listAuditLogs(@RequestBody @Valid ListAuditLogsReq req) {
        return RequestResult.success(auditLogService.listAuditLogs(req));
    }

}

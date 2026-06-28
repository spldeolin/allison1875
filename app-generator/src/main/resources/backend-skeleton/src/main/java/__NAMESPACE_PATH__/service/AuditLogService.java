package __NAMESPACE__.service;

import __NAMESPACE__.entity.*;
import __NAMESPACE__.enums.*;
import java.util.*;
import java.time.*;
import java.math.*;
import com.fasterxml.jackson.annotation.*;
import java.util.stream.*;
import org.springframework.util.*;
import __NAMESPACE__.dto.resp.PageResult;
import __NAMESPACE__.dto.resp.ListAuditLogsResp;
import __NAMESPACE__.dto.req.ListAuditLogsReq;

/**
 * @author Deolin
 */
public interface AuditLogService {

    PageResult<ListAuditLogsResp> listAuditLogs(ListAuditLogsReq req);

}

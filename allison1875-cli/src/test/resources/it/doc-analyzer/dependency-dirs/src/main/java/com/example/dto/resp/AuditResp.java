package com.example.dto.resp;

import lombok.Data;

/**
 * @author test-author 2026-04-29
 */
@Data
public class AuditResp {

    /**
     * 审计ID
     */
    private Long auditId;

    /**
     * 操作类型
     */
    private String actionType;

}

package com.example.dto.req;

import lombok.Data;

/**
 * @author test-author 2026-04-29
 */
@Data
public class CreateReportReq {

    /**
     * 报表名称
     */
    private String reportName;

    /**
     * 报表类型
     */
    private String reportType;

}

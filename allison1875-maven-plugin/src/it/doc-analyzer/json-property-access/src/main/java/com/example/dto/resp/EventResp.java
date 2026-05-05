package com.example.dto.resp;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * @author test-author 2026-04-29
 */
@Data
public class EventResp {

    /**
     * ID
     */
    private Long id;

    /**
     * 事件名称
     */
    private String eventName;

    /**
     * 事件开始时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date startTime;

    /**
     * 只读字段（在响应体中应存在）
     */
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String readOnlyField;

    /**
     * 只写字段（在响应体中应被忽略）
     */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String writeOnlyForResp;

}

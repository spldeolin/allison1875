package com.example.dto.resp;

import com.example.enums.SimpleStatusEnum;
import lombok.Data;

/**
 * @author test-author 2026-04-29
 */
@Data
public class SimpleStatusResp {
    /** ID */
    private String id;
    /** 状态 */
    private SimpleStatusEnum status;
}

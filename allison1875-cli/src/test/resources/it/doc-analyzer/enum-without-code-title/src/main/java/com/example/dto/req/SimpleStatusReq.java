package com.example.dto.req;

import com.example.enums.SimpleStatusEnum;
import lombok.Data;

/**
 * @author test-author 2026-04-29
 */
@Data
public class SimpleStatusReq {

    /**
     * 状态（无getCode/getTitle的枚举，枚举项不出现）
     */
    private SimpleStatusEnum status;

    /**
     * 备注
     */
    private String remark;
}

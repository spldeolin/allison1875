package com.example.dto.resp;

import java.util.List;
import com.example.enums.OperationTypeEnum;
import lombok.Data;

/**
 * 批量更新响应
 *
 * @author test-author 2026-04-29
 */
@Data
public class BatchUpdateResp {

    /**
     * 成功数量
     */
    private Integer successCount;

    /**
     * 操作类型列表（Response中也包含List内枚举）
     */
    private List<OperationTypeEnum> operations;

}

package com.example.dto.req;

import java.util.List;
import com.example.enums.OperationTypeEnum;
import lombok.Data;

/**
 * 批量更新请求
 *
 * @author test-author 2026-04-29
 */
@Data
public class BatchUpdateReq {

    /**
     * 操作类型列表（List内枚举，触发Collection泛型递归分支）
     */
    private List<OperationTypeEnum> operations;

    /**
     * 目标ID列表
     */
    private List<String> targetIds;

}

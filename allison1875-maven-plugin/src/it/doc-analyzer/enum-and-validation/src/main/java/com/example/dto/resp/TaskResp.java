package com.example.dto.resp;

import com.example.enums.TaskStatusEnum;
import lombok.Data;

/**
 * 任务响应
 *
 * @author test-author 2026-04-29
 */
@Data
public class TaskResp {

    /**
     * 任务ID
     */
    private Long id;

    /**
     * 任务标题
     */
    private String title;

    /**
     * 任务状态
     */
    private TaskStatusEnum status;

    /**
     * 优先级
     */
    private Integer priority;

}

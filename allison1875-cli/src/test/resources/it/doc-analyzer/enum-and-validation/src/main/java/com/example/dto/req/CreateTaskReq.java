package com.example.dto.req;

import java.math.BigDecimal;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import com.example.enums.TaskStatusEnum;
import lombok.Data;

/**
 * 创建任务请求
 *
 * @author test-author 2026-04-29
 */
@Data
public class CreateTaskReq {

    /**
     * 任务标题
     */
    @NotBlank
    @Size(min = 1, max = 200)
    private String title;

    /**
     * 任务描述
     */
    @Size(max = 2000)
    private String description;

    /**
     * 任务状态
     */
    @NotNull
    private TaskStatusEnum status;

    /**
     * 优先级 (1-10)
     */
    @NotNull
    @Min(1)
    @Max(10)
    private Integer priority;

    /**
     * 预算金额
     */
    private BigDecimal budget;

    /**
     * 任务编号（正则校验）
     */
    @Pattern(regexp = "^TASK-\\d{6}$")
    private String taskCode;

}

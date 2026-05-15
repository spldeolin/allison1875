package com.example.controller;

import javax.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * expansion变量测试
 *
 * @author test-author 2026-04-29
 */
@RestController
@RequestMapping("/api/expansion")
public class ExpansionController {

    {
        String handler = "/do-task";
        String desc = "执行任务";
        String customKey = "customValue";
        String anotherVar = "anotherValue";
        class Req {
            /** 任务名 */
            @NotBlank
            String taskName;
        }
        class Resp {
            /** 任务ID */
            Long taskId;
        }
    }

}

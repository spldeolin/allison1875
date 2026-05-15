package com.example.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.example.dto.req.CreateTaskReq;
import com.example.dto.resp.TaskResp;

/**
 * 任务管理
 *
 * @author test-author 2026-04-29
 */
@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    /**
     * 创建任务
     */
    @PostMapping
    public TaskResp createTask(@RequestBody CreateTaskReq req) {
        return null;
    }

}

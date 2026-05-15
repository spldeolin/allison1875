package com.example.controller;

import com.spldeolin.allison1875.support.L;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 嵌套DTO列表测试
 *
 * @author test-author 2026-04-29
 */
@RestController
@RequestMapping("/api/classroom")
public class ClassroomController {

    {
        String handler = "/get-classroom";
        String desc = "查询教室详情";
        class Resp {
            /** 教室名称 */
            String classroomName;
            /** 学生列表（嵌套 DTO 标注 @L） */
            @L
            class Student {
                /** 学生姓名 */
                String studentName;
                /** 学号 */
                String studentNo;
            }
            /** 课程列表（嵌套 DTO 标注 @L） */
            @L
            class Course {
                /** 课程名 */
                String courseName;
            }
        }
    }

}

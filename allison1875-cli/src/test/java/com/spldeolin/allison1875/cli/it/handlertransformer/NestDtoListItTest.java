package com.spldeolin.allison1875.cli.it.handlertransformer;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import org.junit.jupiter.api.Test;
import com.spldeolin.allison1875.cli.it.docanalyzer.HandlerTransformerItBaseTest;

/**
 * nest-dto-list 集成测试。
 *
 * 验证嵌套 DTO 标注 @L 时，父类中的字段名被复数化（English.plural），
 * 且字段类型为 List&lt;XxxDTO&gt;。
 *
 * @author Deolin 2026-05-13
 */
public class NestDtoListItTest extends HandlerTransformerItBaseTest {

    @Test
    void test() throws IOException {
        runHandlerTransformer("nest-dto-list");

        // ========== 1. 验证 Controller 被改写 ==========
        File controllerFile = new File(basedir, "src/main/java/com/example/controller/ClassroomController.java");
        assertTrue(controllerFile.exists(), "ClassroomController.java should exist");
        String controllerContent = new String(Files.readAllBytes(controllerFile.toPath()), StandardCharsets.UTF_8);

        // init 块移除
        assertFalse(controllerContent.contains("class Resp"), "inner class Resp should be removed");
        assertFalse(controllerContent.contains("class Student"), "nested class Student should be removed");
        assertFalse(controllerContent.contains("class Course"), "nested class Course should be removed");
        assertTrue(controllerContent.contains("/get-classroom"), "Should contain URL");

        // ========== 2. 验证 Resp DTO ==========
        File respDtoDir = new File(basedir, "src/main/java/com/example/dto/resp");
        assertTrue(respDtoDir.exists(), "resp DTO directory should exist");

        // 嵌套 Student DTO 生成为独立文件
        File[] studentFiles = respDtoDir.listFiles((dir, name) -> name.endsWith(".java") && name.contains("Student"));
        assertTrue(studentFiles != null && studentFiles.length == 1, "Should generate StudentDTO file");

        String studentContent = new String(Files.readAllBytes(studentFiles[0].toPath()), StandardCharsets.UTF_8);
        assertTrue(studentContent.contains("studentName"), "StudentDTO should contain 'studentName'");
        assertTrue(studentContent.contains("studentNo"), "StudentDTO should contain 'studentNo'");

        // 嵌套 Course DTO 生成为独立文件
        File[] courseFiles = respDtoDir.listFiles((dir, name) -> name.endsWith(".java") && name.contains("Course"));
        assertTrue(courseFiles != null && courseFiles.length == 1, "Should generate CourseDTO file");

        // Resp 主 DTO 中字段名应该被复数化（students, courses）
        File[] respFiles = respDtoDir.listFiles(
                (dir, name) -> name.endsWith(".java") && name.contains("GetClassroom") && name.contains("Resp"));
        assertTrue(respFiles != null && respFiles.length == 1, "Should generate GetClassroomResp DTO");

        String respContent = new String(Files.readAllBytes(respFiles[0].toPath()), StandardCharsets.UTF_8);
        assertTrue(respContent.contains("classroomName"), "Resp DTO should contain 'classroomName'");
        // @L 嵌套 DTO 的字段名应为复数
        assertTrue(respContent.contains("students"),
                "Resp DTO should contain plural field name 'students' for @L Student");
        assertTrue(respContent.contains("courses"),
                "Resp DTO should contain plural field name 'courses' for @L Course");
        // 字段类型应为 List
        assertTrue(respContent.contains("List"), "Resp DTO should contain List type for @L nested DTO fields");
    }

}
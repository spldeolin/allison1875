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
 * annotation-resolve-failure 集成测试。
 *
 * <p>验证 Controller 正常情况下的完整转换流程。
 *
 * <p>注：MvcControllerServiceImpl.isController 中 annotation.resolve() 的异常 catch 分支
 * 需要 JavaParser TypeSolver 层面可 resolve 但运行时抛异常的场景，在资源文件编译约束下
 * 难以构造（未知注解会导致 javac 编译失败）。该分支的实际覆盖更依赖单元测试或 mock 环境。
 *
 * @author Deolin 2026-05-15
 */
public class AnnotationResolveFailureItTest extends HandlerTransformerItBaseTest {

    @Test
    void test() throws IOException {
        runHandlerTransformer("annotation-resolve-failure");

        // ========== 1. 验证 Controller 被改写（不因注解 resolve 失败而终止） ==========
        File controllerFile = new File(basedir, "src/main/java/com/example/controller/AnnoFailController.java");
        assertTrue(controllerFile.exists(), "AnnoFailController.java should exist");
        String controllerContent = new String(Files.readAllBytes(controllerFile.toPath()), StandardCharsets.UTF_8);

        // init 块被移除
        assertFalse(controllerContent.contains("String handler ="), "init block should be removed");
        assertFalse(controllerContent.contains("class Req"), "inner class Req should be removed");
        assertFalse(controllerContent.contains("class Resp"), "inner class Resp should be removed");

        // handler 方法正常生成
        assertTrue(controllerContent.contains("/create-task"), "Should contain URL '/create-task'");
        assertTrue(controllerContent.contains("createTask"), "Should contain method 'createTask'");

        // ========== 2. 验证 Req DTO 正常生成 ==========
        File reqDtoDir = new File(basedir, "src/main/java/com/example/dto/req");
        assertTrue(reqDtoDir.exists(), "req DTO directory should exist");

        File[] reqFiles = reqDtoDir.listFiles(
                (dir, name) -> name.endsWith(".java") && name.contains("CreateTask") && name.contains("Req"));
        assertTrue(reqFiles != null && reqFiles.length == 1, "Should generate exactly 1 Req DTO for CreateTask");

        // ========== 3. 验证 Resp DTO 正常生成 ==========
        File respDtoDir = new File(basedir, "src/main/java/com/example/dto/resp");
        assertTrue(respDtoDir.exists(), "resp DTO directory should exist");

        File[] respFiles = respDtoDir.listFiles(
                (dir, name) -> name.endsWith(".java") && name.contains("CreateTask") && name.contains("Resp"));
        assertTrue(respFiles != null && respFiles.length == 1, "Should generate exactly 1 Resp DTO for CreateTask");

        // ========== 4. 验证 Service 正常生成 ==========
        File serviceDir = new File(basedir, "src/main/java/com/example/service");
        assertTrue(serviceDir.exists(), "service directory should exist");

        File[] serviceFiles = serviceDir.listFiles(
                (dir, name) -> name.endsWith(".java") && name.contains("CreateTask") && name.contains("Service"));
        assertTrue(serviceFiles != null && serviceFiles.length == 1,
                "Should generate exactly 1 Service for CreateTask");
    }

}

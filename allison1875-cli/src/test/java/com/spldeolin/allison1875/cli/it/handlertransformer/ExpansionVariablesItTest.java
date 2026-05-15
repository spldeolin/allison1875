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
 * expansion-variables 集成测试。
 *
 * <p>验证 init 块中除 handler/desc 外的自定义字符串变量被正确收集到 expansion Map 中，
 * 不影响正常转换流程。
 *
 * @author Deolin 2026-05-15
 */
public class ExpansionVariablesItTest extends HandlerTransformerItBaseTest {

    @Test
    void test() throws IOException {
        runHandlerTransformer("expansion-variables");

        // ========== 1. 验证 Controller 正常改写 ==========
        File controllerFile = new File(basedir, "src/main/java/com/example/controller/ExpansionController.java");
        assertTrue(controllerFile.exists(), "ExpansionController.java should exist");
        String controllerContent = new String(Files.readAllBytes(controllerFile.toPath()), StandardCharsets.UTF_8);

        // init 块被移除
        assertFalse(controllerContent.contains("String handler ="), "init block should be removed");
        assertFalse(controllerContent.contains("customKey"), "expansion variable 'customKey' should be removed");
        assertFalse(controllerContent.contains("anotherVar"), "expansion variable 'anotherVar' should be removed");
        assertFalse(controllerContent.contains("class Req"), "inner class Req should be removed");
        assertFalse(controllerContent.contains("class Resp"), "inner class Resp should be removed");

        // handler 方法正常生成
        assertTrue(controllerContent.contains("/do-task"), "Should contain URL '/do-task'");
        assertTrue(controllerContent.contains("doTask"), "Should contain method 'doTask'");

        // ========== 2. 验证 Req DTO 正常生成 ==========
        File reqDtoDir = new File(basedir, "src/main/java/com/example/dto/req");
        assertTrue(reqDtoDir.exists(), "req DTO directory should exist");

        File[] reqFiles = reqDtoDir.listFiles(
                (dir, name) -> name.endsWith(".java") && name.contains("DoTask") && name.contains("Req"));
        assertTrue(reqFiles != null && reqFiles.length == 1, "Should generate exactly 1 Req DTO for DoTask");

        String reqContent = new String(Files.readAllBytes(reqFiles[0].toPath()), StandardCharsets.UTF_8);
        assertTrue(reqContent.contains("taskName"), "Req DTO should contain field 'taskName'");

        // ========== 3. 验证 Resp DTO 正常生成 ==========
        File respDtoDir = new File(basedir, "src/main/java/com/example/dto/resp");
        assertTrue(respDtoDir.exists(), "resp DTO directory should exist");

        File[] respFiles = respDtoDir.listFiles(
                (dir, name) -> name.endsWith(".java") && name.contains("DoTask") && name.contains("Resp"));
        assertTrue(respFiles != null && respFiles.length == 1, "Should generate exactly 1 Resp DTO for DoTask");

        // ========== 4. 验证 Service 正常生成 ==========
        File serviceDir = new File(basedir, "src/main/java/com/example/service");
        assertTrue(serviceDir.exists(), "service directory should exist");

        File[] serviceFiles = serviceDir.listFiles(
                (dir, name) -> name.endsWith(".java") && name.contains("DoTask") && name.contains("Service"));
        assertTrue(serviceFiles != null && serviceFiles.length == 1, "Should generate exactly 1 Service for DoTask");
    }

}

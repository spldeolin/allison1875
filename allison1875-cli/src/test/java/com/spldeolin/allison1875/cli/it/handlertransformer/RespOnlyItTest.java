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
 * resp-only 集成测试。
 *
 * <p>验证 init 块只有 Resp、没有 Req 时，生成无参数但有返回值的 handler。
 *
 * @author Deolin 2026-05-13
 */
public class RespOnlyItTest extends HandlerTransformerItBaseTest {

    @Test
    void test() throws IOException {
        runHandlerTransformer("resp-only");

        // ========== 1. 验证 Controller 被改写 ==========
        File controllerFile = new File(basedir, "src/main/java/com/example/controller/ConfigController.java");
        assertTrue(controllerFile.exists(), "ConfigController.java should exist");
        String controllerContent = new String(Files.readAllBytes(controllerFile.toPath()), StandardCharsets.UTF_8);

        // init 块应该被移除
        assertFalse(controllerContent.contains("String handler = \"/get-config\""),
                "init block variable 'handler' should be removed");
        assertFalse(controllerContent.contains("class Resp"), "inner class Resp should be removed from controller");

        // handler 方法生成
        assertTrue(controllerContent.contains("/get-config"), "Controller should contain URL '/get-config'");
        assertTrue(controllerContent.contains("getConfig"), "Controller should contain method 'getConfig'");

        // 无 @RequestBody（没有 Req 类）
        assertFalse(controllerContent.contains("@RequestBody"), "Handler without Req should not have @RequestBody");

        // ========== 2. 验证 Resp DTO 生成 ==========
        File respDtoDir = new File(basedir, "src/main/java/com/example/dto/resp");
        assertTrue(respDtoDir.exists(), "resp DTO directory should exist");

        File[] respFiles = respDtoDir.listFiles(
                (dir, name) -> name.endsWith(".java") && name.contains("GetConfig") && name.contains("Resp"));
        assertTrue(respFiles != null && respFiles.length == 1, "Should generate exactly 1 Resp DTO file for GetConfig");

        String respContent = new String(Files.readAllBytes(respFiles[0].toPath()), StandardCharsets.UTF_8);
        assertTrue(respContent.contains("appName"), "Resp DTO should contain field 'appName'");
        assertTrue(respContent.contains("version"), "Resp DTO should contain field 'version'");
        assertTrue(respContent.contains("maintenance"), "Resp DTO should contain field 'maintenance'");

        // ========== 3. 不应生成 Req DTO ==========
        File reqDtoDir = new File(basedir, "src/main/java/com/example/dto/req");
        if (reqDtoDir.exists()) {
            File[] reqFiles = reqDtoDir.listFiles((dir, name) -> name.endsWith(".java"));
            assertTrue(reqFiles == null || reqFiles.length == 0, "No Req DTO should be generated");
        }
    }

}
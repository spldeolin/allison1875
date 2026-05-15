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
 * no-handler-skip 集成测试。
 *
 * 验证 init 块不包含 handler 变量时，该 init 块被跳过，Controller 保持不变。
 *
 * @author Deolin 2026-05-13
 */
public class NoHandlerSkipItTest extends HandlerTransformerItBaseTest {

    @Test
    void test() throws IOException {
        runHandlerTransformer("no-handler-skip");

        // ========== 1. 验证 Controller 保持不变 ==========
        File controllerFile = new File(basedir, "src/main/java/com/example/controller/SkipController.java");
        assertTrue(controllerFile.exists(), "SkipController.java should exist");
        String controllerContent = new String(Files.readAllBytes(controllerFile.toPath()), StandardCharsets.UTF_8);

        // init 块应该保持不变（因为没有 handler 变量，工具应跳过）
        assertTrue(controllerContent.contains("这个 init 块缺少 handler 变量"),
                "init block should remain unchanged (no handler → skip)");
        assertTrue(controllerContent.contains("class Req"), "inner class Req should remain (init block not processed)");

        // 不应生成任何 handler 方法
        assertFalse(controllerContent.contains("PostMapping"), "No handler method should be generated");
        assertFalse(controllerContent.contains("GetMapping"), "No handler method should be generated");

        // ========== 2. 验证不应生成 Service 文件 ==========
        File serviceDir = new File(basedir, "src/main/java/com/example/service");
        if (serviceDir.exists()) {
            File[] serviceFiles = serviceDir.listFiles((dir, name) -> name.endsWith(".java"));
            assertTrue(serviceFiles == null || serviceFiles.length == 0,
                    "No Service files should be generated when handler is skipped");
        }

        // ========== 3. 验证不应生成 DTO 文件 ==========
        File reqDtoDir = new File(basedir, "src/main/java/com/example/dto/req");
        if (reqDtoDir.exists()) {
            File[] dtoFiles = reqDtoDir.listFiles((dir, name) -> name.endsWith(".java"));
            assertTrue(dtoFiles == null || dtoFiles.length == 0, "No DTO files should be generated");
        }
    }

}
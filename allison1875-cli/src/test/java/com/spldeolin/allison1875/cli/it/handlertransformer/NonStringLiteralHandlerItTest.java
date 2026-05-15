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
 * non-string-literal-handler 集成测试。
 *
 * <p>验证 init 块中 handler 变量的初始值不是字符串字面量（如方法调用）时，该 init 块被跳过。
 *
 * @author Deolin 2026-05-15
 */
public class NonStringLiteralHandlerItTest extends HandlerTransformerItBaseTest {

    @Test
    void test() throws IOException {
        runHandlerTransformer("non-string-literal-handler");

        // ========== 1. 验证 Controller 保持不变（init 块被跳过） ==========
        File controllerFile = new File(basedir, "src/main/java/com/example/controller/NonLiteralController.java");
        assertTrue(controllerFile.exists(), "NonLiteralController.java should exist");
        String controllerContent = new String(Files.readAllBytes(controllerFile.toPath()), StandardCharsets.UTF_8);

        // init 块应该保持不变
        assertTrue(controllerContent.contains("String handler = getUrl()"),
                "Non-string-literal handler should remain unchanged (init block skipped)");
        assertTrue(controllerContent.contains("class Req"), "inner class Req should remain (init block not processed)");

        // 不应生成 handler 方法
        assertFalse(controllerContent.contains("PostMapping"), "No @PostMapping should be generated");
        assertFalse(controllerContent.contains("GetMapping"), "No @GetMapping should be generated");

        // ========== 2. 验证不生成 Service 文件 ==========
        File serviceDir = new File(basedir, "src/main/java/com/example/service");
        if (serviceDir.exists()) {
            File[] serviceFiles = serviceDir.listFiles((dir, name) -> name.endsWith(".java"));
            assertTrue(serviceFiles == null || serviceFiles.length == 0,
                    "No Service files should be generated when handler is non-string-literal");
        }

        // ========== 3. 验证不生成 DTO 文件 ==========
        File reqDtoDir = new File(basedir, "src/main/java/com/example/dto/req");
        if (reqDtoDir.exists()) {
            File[] dtoFiles = reqDtoDir.listFiles((dir, name) -> name.endsWith(".java"));
            assertTrue(dtoFiles == null || dtoFiles.length == 0,
                    "No DTO files should be generated when handler is non-string-literal");
        }
    }

}

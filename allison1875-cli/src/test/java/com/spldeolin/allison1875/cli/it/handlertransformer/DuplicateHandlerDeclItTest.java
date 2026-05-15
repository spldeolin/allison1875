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
 * duplicate-handler-decl 集成测试。
 *
 * <p>验证 init 块中 handler 变量重复声明时，只取第一次的值，后续声明被忽略并 warn。
 *
 * @author Deolin 2026-05-15
 */
public class DuplicateHandlerDeclItTest extends HandlerTransformerItBaseTest {

    @Test
    void test() throws IOException {
        runHandlerTransformer("duplicate-handler-decl");

        // ========== 1. 验证 Controller 被改写，handler URL 取第一次声明的值 ==========
        File controllerFile = new File(basedir, "src/main/java/com/example/controller/DupController.java");
        assertTrue(controllerFile.exists(), "DupController.java should exist");
        String controllerContent = new String(Files.readAllBytes(controllerFile.toPath()), StandardCharsets.UTF_8);

        // init 块中重复声明的 handler 应该被移除
        assertFalse(controllerContent.contains("String handler ="), "init block should be removed");

        // URL 应为第一次声明的值 /first-url，而非 /second-url
        assertTrue(controllerContent.contains("/first-url"), "Should contain first handler URL '/first-url'");
        assertFalse(controllerContent.contains("/second-url"), "Should NOT contain second handler URL '/second-url'");

        // handler 方法应该生成
        assertTrue(controllerContent.contains("PostMapping"), "Controller should contain @PostMapping");

        // ========== 2. 验证 Service / DTO 正常生成 ==========
        File serviceDir = new File(basedir, "src/main/java/com/example/service");
        assertTrue(serviceDir.exists(), "service directory should exist");

        File[] serviceFiles = serviceDir.listFiles((dir, name) -> name.endsWith(".java") && name.contains("FirstUrl"));
        assertTrue(serviceFiles != null && serviceFiles.length == 1,
                "Should generate Service based on first URL 'FirstUrl'");

        // DTO 也应基于 first-url 命名
        File reqDtoDir = new File(basedir, "src/main/java/com/example/dto/req");
        assertTrue(reqDtoDir.exists(), "req DTO directory should exist");
        File[] reqFiles = reqDtoDir.listFiles((dir, name) -> name.endsWith(".java") && name.contains("FirstUrl"));
        assertTrue(reqFiles != null && reqFiles.length == 1, "Should generate Req DTO based on first URL 'FirstUrl'");
    }

}

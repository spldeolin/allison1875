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
 * handler-alias 集成测试。
 *
 * 验证 init 块使用 h/d 别名代替 handler/desc 时，工具仍能正确解析。
 *
 * @author Deolin 2026-05-13
 */
public class HandlerAliasItTest extends HandlerTransformerItBaseTest {

    @Test
    void test() throws IOException {
        runHandlerTransformer("handler-alias");

        // ========== 1. 验证 Controller 被改写 ==========
        File controllerFile = new File(basedir, "src/main/java/com/example/controller/AliasController.java");
        assertTrue(controllerFile.exists(), "AliasController.java should exist");
        String controllerContent = new String(Files.readAllBytes(controllerFile.toPath()), StandardCharsets.UTF_8);

        // init 块移除（h/d 变量声明应被清除）
        assertFalse(controllerContent.contains("String h = \"/do-something\""),
                "init block alias 'h' should be removed");
        assertFalse(controllerContent.contains("String d = \"执行操作\""), "init block alias 'd' should be removed");
        assertFalse(controllerContent.contains("class Req"), "inner class Req should be removed");

        // handler 方法生成
        assertTrue(controllerContent.contains("/do-something"), "Should contain URL '/do-something'");
        assertTrue(controllerContent.contains("doSomething"), "Should contain method 'doSomething'");
        assertTrue(controllerContent.contains("PostMapping"), "Should contain @PostMapping");

        // desc 应该出现在 javadoc 中
        assertTrue(controllerContent.contains("执行操作"), "Handler javadoc should contain desc '执行操作'");

        // ========== 2. 验证 Service 正常生成 ==========
        File serviceDir = new File(basedir, "src/main/java/com/example/service");
        assertTrue(serviceDir.exists(), "service directory should exist");
        File[] serviceFiles = serviceDir.listFiles(
                (dir, name) -> name.endsWith(".java") && name.contains("DoSomething"));
        assertTrue(serviceFiles != null && serviceFiles.length == 1, "Should generate 1 Service file");
    }

}
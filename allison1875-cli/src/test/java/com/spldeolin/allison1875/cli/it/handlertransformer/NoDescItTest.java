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
 * no-desc 集成测试。
 *
 * 验证 init 块只有 handler 无 desc 时，工具使用默认描述完成转换而非报错。
 *
 * @author Deolin 2026-05-13
 */
public class NoDescItTest extends HandlerTransformerItBaseTest {

    @Test
    void test() throws IOException {
        runHandlerTransformer("no-desc");

        // ========== 1. 验证 Controller 被改写 ==========
        File controllerFile = new File(basedir, "src/main/java/com/example/controller/LogController.java");
        assertTrue(controllerFile.exists(), "LogController.java should exist");
        String controllerContent = new String(Files.readAllBytes(controllerFile.toPath()), StandardCharsets.UTF_8);

        // init 块被替换
        assertFalse(controllerContent.contains("String handler = \"/clear-log\""), "init block should be removed");

        // handler 方法生成
        assertTrue(controllerContent.contains("/clear-log"), "Should contain URL '/clear-log'");
        assertTrue(controllerContent.contains("clearLog"), "Should contain method 'clearLog'");

        // 默认描述 "未指定描述" 应出现在 handler 的 Javadoc 中
        assertTrue(controllerContent.contains("未指定描述"),
                "Handler javadoc should contain default description '未指定描述'");

        // ========== 2. 验证 Service 生成 ==========
        File serviceDir = new File(basedir, "src/main/java/com/example/service");
        assertTrue(serviceDir.exists(), "service directory should exist");
        File[] serviceFiles = serviceDir.listFiles((dir, name) -> name.endsWith(".java") && name.contains("ClearLog"));
        assertTrue(serviceFiles != null && serviceFiles.length == 1,
                "Should generate 1 Service file, found: " + (serviceFiles == null ? "null" : serviceFiles.length));
    }

}
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
 * no-valid-init-dec 集成测试。
 *
 * <p>验证项目中所有 Controller 的 init 块均不包含有效 handler 变量时，工具正常结束不报错，
 * Controller 保持不变，不生成 Service/DTO。
 *
 * @author Deolin 2026-05-15
 */
public class NoValidInitDecItTest extends HandlerTransformerItBaseTest {

    @Test
    void test() throws IOException {
        runHandlerTransformer("no-valid-init-dec");

        // ========== 1. 验证 Controller 保持不变 ==========
        File controllerFile = new File(basedir, "src/main/java/com/example/controller/NoValidController.java");
        assertTrue(controllerFile.exists(), "NoValidController.java should exist");
        String controllerContent = new String(Files.readAllBytes(controllerFile.toPath()), StandardCharsets.UTF_8);

        // init 块应该保持不变（没有有效的 handler 变量）
        assertTrue(controllerContent.contains("无handler变量的init块"),
                "init block should remain unchanged (no valid handler)");
        assertTrue(controllerContent.contains("class Req"), "inner class Req should remain (init block not processed)");

        // 不应生成 handler 方法
        assertFalse(controllerContent.contains("PostMapping"), "No @PostMapping should be generated");
        assertFalse(controllerContent.contains("GetMapping"), "No @GetMapping should be generated");

        // ========== 2. 验证不生成 Service 文件 ==========
        File serviceDir = new File(basedir, "src/main/java/com/example/service");
        if (serviceDir.exists()) {
            File[] serviceFiles = serviceDir.listFiles((dir, name) -> name.endsWith(".java"));
            assertTrue(serviceFiles == null || serviceFiles.length == 0,
                    "No Service files should be generated when no valid init dec exists");
        }

        // ========== 3. 验证不生成 DTO 文件 ==========
        File reqDtoDir = new File(basedir, "src/main/java/com/example/dto/req");
        if (reqDtoDir.exists()) {
            File[] dtoFiles = reqDtoDir.listFiles((dir, name) -> name.endsWith(".java"));
            assertTrue(dtoFiles == null || dtoFiles.length == 0,
                    "No DTO files should be generated when no valid init dec exists");
        }
    }

}

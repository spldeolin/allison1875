package com.spldeolin.allison1875.cli.it.handlertransformer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import org.junit.jupiter.api.Test;
import com.spldeolin.allison1875.cli.it.docanalyzer.HandlerTransformerItBaseTest;

/**
 * multi-controller 集成测试。
 *
 * 验证项目包含多个 Controller 文件时，每个都被独立检测和转换。
 *
 * @author Deolin 2026-05-13
 */
public class MultiControllerItTest extends HandlerTransformerItBaseTest {

    @Test
    void test() throws IOException {
        runHandlerTransformer("multi-controller");

        // ========== First Controller ==========
        File firstFile = new File(basedir, "src/main/java/com/example/controller/FirstController.java");
        assertTrue(firstFile.exists(), "FirstController.java should exist");
        String firstContent = new String(Files.readAllBytes(firstFile.toPath()), StandardCharsets.UTF_8);

        assertFalse(firstContent.contains("String handler ="), "First init block should be removed");
        assertTrue(firstContent.contains("/do-first"), "First should contain URL '/do-first'");
        assertTrue(firstContent.contains("doFirst"), "First should contain method 'doFirst'");
        assertTrue(firstContent.contains("PostMapping"), "First should have @PostMapping");

        // ========== Second Controller ==========
        File secondFile = new File(basedir, "src/main/java/com/example/controller/SecondController.java");
        assertTrue(secondFile.exists(), "SecondController.java should exist");
        String secondContent = new String(Files.readAllBytes(secondFile.toPath()), StandardCharsets.UTF_8);

        assertFalse(secondContent.contains("String handler ="), "Second init block should be removed");
        assertTrue(secondContent.contains("/do-second"), "Second should contain URL '/do-second'");
        assertTrue(secondContent.contains("doSecond"), "Second should contain method 'doSecond'");
        assertTrue(secondContent.contains("PostMapping"), "Second should have @PostMapping");

        // ========== 各自生成独立 Service ==========
        File serviceDir = new File(basedir, "src/main/java/com/example/service");
        assertTrue(serviceDir.exists(), "service directory should exist");

        File[] serviceFiles = serviceDir.listFiles((dir, name) -> name.endsWith(".java"));
        assertNotNull(serviceFiles, "Service files should exist");

        int firstServiceCount = 0;
        int secondServiceCount = 0;
        for (File f : serviceFiles) {
            if (f.getName().contains("DoFirst")) {
                firstServiceCount++;
            }
            if (f.getName().contains("DoSecond")) {
                secondServiceCount++;
            }
        }
        assertEquals(1, firstServiceCount, "Should generate Service for DoFirst");
        assertEquals(1, secondServiceCount, "Should generate Service for DoSecond");

        // ========== 各自生成 DTO ==========
        File reqDtoDir = new File(basedir, "src/main/java/com/example/dto/req");
        assertTrue(reqDtoDir.exists(), "req DTO directory should exist");
        File[] reqFiles = reqDtoDir.listFiles((dir, name) -> name.endsWith(".java"));
        assertTrue(reqFiles != null && reqFiles.length >= 2,
                "Should generate at least 2 Req DTOs (one per controller)");

        File respDtoDir = new File(basedir, "src/main/java/com/example/dto/resp");
        assertTrue(respDtoDir.exists(), "resp DTO directory should exist");
        File[] respFiles = respDtoDir.listFiles((dir, name) -> name.endsWith(".java"));
        assertTrue(respFiles != null && respFiles.length >= 2,
                "Should generate at least 2 Resp DTOs (one per controller)");
    }

}
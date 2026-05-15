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
 * nest-dto-custom-annotations 集成测试。
 *
 * <p>验证嵌套 DTO 上的自定义注解（非 @L/@P）被迁移到父 DTO 的字段上。
 *
 * @author Deolin 2026-05-15
 */
public class NestDtoCustomAnnotationsItTest extends HandlerTransformerItBaseTest {

    @Test
    void test() throws IOException {
        runHandlerTransformer("nest-dto-custom-annotations");

        // ========== 1. 验证 Controller 被改写 ==========
        File controllerFile = new File(basedir, "src/main/java/com/example/controller/CustomAnnoController.java");
        assertTrue(controllerFile.exists(), "CustomAnnoController.java should exist");
        String controllerContent = new String(Files.readAllBytes(controllerFile.toPath()), StandardCharsets.UTF_8);

        assertFalse(controllerContent.contains("class Req"), "inner class Req should be removed");
        assertFalse(controllerContent.contains("class Detail"), "nested class Detail should be removed");
        assertTrue(controllerContent.contains("/create-item"), "Should contain URL");

        // ========== 2. 验证嵌套 Detail 生成为独立 DTO ==========
        File reqDtoDir = new File(basedir, "src/main/java/com/example/dto/req");
        assertTrue(reqDtoDir.exists(), "req DTO directory should exist");

        File[] detailFiles = reqDtoDir.listFiles((dir, name) -> name.endsWith(".java") && name.contains("Detail"));
        assertTrue(detailFiles != null && detailFiles.length == 1, "Should generate DetailDTO file");

        // ========== 3. 验证 Req 主 DTO 中嵌套字段含 @Deprecated（自定义注解被迁移） ==========
        File[] reqFiles = reqDtoDir.listFiles(
                (dir, name) -> name.endsWith(".java") && name.contains("CreateItem") && name.contains("Req"));
        assertTrue(reqFiles != null && reqFiles.length == 1, "Should generate CreateItemReq DTO");

        String reqContent = new String(Files.readAllBytes(reqFiles[0].toPath()), StandardCharsets.UTF_8);
        assertTrue(reqContent.contains("itemName"), "Req should contain 'itemName'");
        // 自定义注解 @Deprecated 应被迁移到父 DTO 的字段上
        assertTrue(reqContent.contains("Deprecated"),
                "Req DTO field for Detail should have @Deprecated annotation migrated from nested class");
    }

}

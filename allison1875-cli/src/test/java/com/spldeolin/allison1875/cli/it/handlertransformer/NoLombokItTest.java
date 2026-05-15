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
 * no-lombok 集成测试。
 *
 * 验证 isDataModelWithoutLombok=true 时，生成的 DTO 包含 getter/setter 方法
 * 而非 Lombok 注解（@Data、@Accessors 等）。
 *
 * @author Deolin 2026-05-13
 */
public class NoLombokItTest extends HandlerTransformerItBaseTest {

    @Test
    void test() throws IOException {
        runHandlerTransformer("no-lombok");

        // ========== 1. 验证 Controller 被改写 ==========
        File controllerFile = new File(basedir, "src/main/java/com/example/controller/NoLombokController.java");
        assertTrue(controllerFile.exists(), "NoLombokController.java should exist");
        String controllerContent = new String(Files.readAllBytes(controllerFile.toPath()), StandardCharsets.UTF_8);

        // init 块移除
        assertFalse(controllerContent.contains("String handler ="), "init block should be removed");
        assertTrue(controllerContent.contains("/create-item"), "Should contain URL");

        // ========== 2. 验证 Req DTO（不含 Lombok） ==========
        File reqDtoDir = new File(basedir, "src/main/java/com/example/dto/req");
        assertTrue(reqDtoDir.exists(), "req DTO directory should exist");
        File[] reqFiles = reqDtoDir.listFiles(
                (dir, name) -> name.endsWith(".java") && name.contains("CreateItem") && name.contains("Req"));
        assertTrue(reqFiles != null && reqFiles.length == 1, "Should generate 1 Req DTO");

        String reqContent = new String(Files.readAllBytes(reqFiles[0].toPath()), StandardCharsets.UTF_8);
        // 应包含 getter/setter 方法
        assertTrue(reqContent.contains("getItemName"), "Req DTO should contain getter 'getItemName'");
        assertTrue(reqContent.contains("setItemName"), "Req DTO should contain setter 'setItemName'");
        assertTrue(reqContent.contains("getQuantity"), "Req DTO should contain getter 'getQuantity'");
        assertTrue(reqContent.contains("setQuantity"), "Req DTO should contain setter 'setQuantity'");
        // 不应包含 Lombok 注解
        assertFalse(reqContent.contains("@Data"), "Req DTO should NOT contain @Data (no Lombok)");
        assertFalse(reqContent.contains("@Accessors"), "Req DTO should NOT contain @Accessors (no Lombok)");

        // ========== 3. 验证 Resp DTO（不含 Lombok） ==========
        File respDtoDir = new File(basedir, "src/main/java/com/example/dto/resp");
        assertTrue(respDtoDir.exists(), "resp DTO directory should exist");
        File[] respFiles = respDtoDir.listFiles(
                (dir, name) -> name.endsWith(".java") && name.contains("CreateItem") && name.contains("Resp"));
        assertTrue(respFiles != null && respFiles.length == 1, "Should generate 1 Resp DTO");

        String respContent = new String(Files.readAllBytes(respFiles[0].toPath()), StandardCharsets.UTF_8);
        assertTrue(respContent.contains("getItemId"), "Resp DTO should contain getter 'getItemId'");
        assertTrue(respContent.contains("setItemId"), "Resp DTO should contain setter 'setItemId'");
        assertFalse(respContent.contains("@Data"), "Resp DTO should NOT contain @Data (no Lombok)");

        // toString, equals, hashCode
        assertTrue(respContent.contains("toString"), "Resp DTO should contain toString method");
        assertTrue(respContent.contains("equals"), "Resp DTO should contain equals method");
        assertTrue(respContent.contains("hashCode"), "Resp DTO should contain hashCode method");
    }

}
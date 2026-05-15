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
 * req-only 集成测试。
 *
 * <p>验证 init 块只有 Req、没有 Resp 时，生成有 @RequestBody 参数但 void 返回的 handler。
 *
 * @author Deolin 2026-05-13
 */
public class ReqOnlyItTest extends HandlerTransformerItBaseTest {

    @Test
    void test() throws IOException {
        runHandlerTransformer("req-only");

        // ========== 1. 验证 Controller 被改写 ==========
        File controllerFile = new File(basedir, "src/main/java/com/example/controller/NotificationController.java");
        assertTrue(controllerFile.exists(), "NotificationController.java should exist");
        String controllerContent = new String(Files.readAllBytes(controllerFile.toPath()), StandardCharsets.UTF_8);

        // init 块应该被移除
        assertFalse(controllerContent.contains("String handler = \"/send-notification\""),
                "init block variable 'handler' should be removed");
        assertFalse(controllerContent.contains("class Req"), "inner class Req should be removed from controller");

        // handler 方法生成
        assertTrue(controllerContent.contains("PostMapping"), "Controller should contain @PostMapping");
        assertTrue(controllerContent.contains("/send-notification"),
                "Controller should contain URL '/send-notification'");
        assertTrue(controllerContent.contains("sendNotification"),
                "Controller should contain method 'sendNotification'");

        // 有 @RequestBody（因为有 Req 类）
        assertTrue(controllerContent.contains("RequestBody"), "Handler with Req should have @RequestBody");

        // handler 方法返回 void
        assertTrue(controllerContent.contains("void"), "Handler without Resp should return void");

        // ========== 2. 验证 Req DTO 生成 ==========
        File reqDtoDir = new File(basedir, "src/main/java/com/example/dto/req");
        assertTrue(reqDtoDir.exists(), "req DTO directory should exist");

        File[] reqFiles = reqDtoDir.listFiles(
                (dir, name) -> name.endsWith(".java") && name.contains("SendNotification") && name.contains("Req"));
        assertTrue(reqFiles != null && reqFiles.length == 1,
                "Should generate exactly 1 Req DTO file for SendNotification");

        String reqContent = new String(Files.readAllBytes(reqFiles[0].toPath()), StandardCharsets.UTF_8);
        assertTrue(reqContent.contains("receiverId"), "Req DTO should contain field 'receiverId'");
        assertTrue(reqContent.contains("content"), "Req DTO should contain field 'content'");
        assertTrue(reqContent.contains("NotNull"), "Req DTO should preserve @NotNull");
        assertTrue(reqContent.contains("NotBlank"), "Req DTO should preserve @NotBlank");

        // ========== 3. 不应生成 Resp DTO ==========
        File respDtoDir = new File(basedir, "src/main/java/com/example/dto/resp");
        if (respDtoDir.exists()) {
            File[] respFiles = respDtoDir.listFiles((dir, name) -> name.endsWith(".java"));
            assertTrue(respFiles == null || respFiles.length == 0, "No Resp DTO should be generated");
        }
    }

}

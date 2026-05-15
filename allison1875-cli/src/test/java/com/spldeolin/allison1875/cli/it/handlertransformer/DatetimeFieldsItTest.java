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
 * datetime-fields 集成测试。
 *
 * 验证 Req/Resp 中的 Date、LocalDateTime、LocalDate、LocalTime 字段
 * 自动附加 @JsonFormat 注解。
 *
 * @author Deolin 2026-05-13
 */
public class DatetimeFieldsItTest extends HandlerTransformerItBaseTest {

    @Test
    void test() throws IOException {
        runHandlerTransformer("datetime-fields");

        // ========== 1. 验证 Controller 被改写 ==========
        File controllerFile = new File(basedir, "src/main/java/com/example/controller/EventController.java");
        assertTrue(controllerFile.exists(), "EventController.java should exist");
        String controllerContent = new String(Files.readAllBytes(controllerFile.toPath()), StandardCharsets.UTF_8);

        // init 块移除
        assertFalse(controllerContent.contains("String handler ="), "init block should be removed");
        assertTrue(controllerContent.contains("/create-event"), "Should contain URL");
        assertTrue(controllerContent.contains("createEvent"), "Should contain method name");

        // ========== 2. 验证 Req DTO ==========
        File reqDtoDir = new File(basedir, "src/main/java/com/example/dto/req");
        assertTrue(reqDtoDir.exists(), "req DTO directory should exist");
        File[] reqFiles = reqDtoDir.listFiles(
                (dir, name) -> name.endsWith(".java") && name.contains("CreateEvent") && name.contains("Req"));
        assertTrue(reqFiles != null && reqFiles.length == 1, "Should generate 1 Req DTO");

        String reqContent = new String(Files.readAllBytes(reqFiles[0].toPath()), StandardCharsets.UTF_8);
        // Date / LocalDateTime 字段应有 @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        assertTrue(reqContent.contains("JsonFormat"), "Req DTO should contain @JsonFormat annotation");
        assertTrue(reqContent.contains("yyyy-MM-dd HH:mm:ss"),
                "Date/LocalDateTime field should have pattern 'yyyy-MM-dd HH:mm:ss'");
        // LocalDate 字段应有 @JsonFormat(pattern = "yyyy-MM-dd")
        assertTrue(reqContent.contains("yyyy-MM-dd"), "LocalDate field should have pattern 'yyyy-MM-dd'");
        // LocalTime 字段应有 @JsonFormat(pattern = "HH:mm:ss")
        assertTrue(reqContent.contains("HH:mm:ss"), "LocalTime field should have pattern 'HH:mm:ss'");

        // ========== 3. 验证 Resp DTO ==========
        File respDtoDir = new File(basedir, "src/main/java/com/example/dto/resp");
        assertTrue(respDtoDir.exists(), "resp DTO directory should exist");
        File[] respFiles = respDtoDir.listFiles(
                (dir, name) -> name.endsWith(".java") && name.contains("CreateEvent") && name.contains("Resp"));
        assertTrue(respFiles != null && respFiles.length == 1, "Should generate 1 Resp DTO");

        String respContent = new String(Files.readAllBytes(respFiles[0].toPath()), StandardCharsets.UTF_8);
        assertTrue(respContent.contains("JsonFormat"),
                "Resp DTO should also contain @JsonFormat for Date/LocalDateTime fields");
    }

}
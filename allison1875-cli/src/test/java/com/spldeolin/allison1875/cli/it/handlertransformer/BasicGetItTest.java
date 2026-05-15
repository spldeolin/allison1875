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
 * basic-get 集成测试。
 *
 * 验证 handler-transformer 对 @GetUrlQuery 标注的 Req 类生成 @GetMapping handler，
 * Req 字段作为 query params 而非 @RequestBody。
 *
 * @author Deolin 2025-05-13
 */
public class BasicGetItTest extends HandlerTransformerItBaseTest {

    @Test
    void test() throws IOException {
        runHandlerTransformer("basic-get");

        // ========== 1. 验证 Controller 被改写 ==========
        File controllerFile = new File(basedir, "src/main/java/com/example/controller/UserController.java");
        assertTrue(controllerFile.exists(), "UserController.java should exist");
        String controllerContent = new String(Files.readAllBytes(controllerFile.toPath()), StandardCharsets.UTF_8);

        // init 块应该被移除
        assertFalse(controllerContent.contains("String handler = \"/list-users\""),
                "init block variable 'handler' should be removed");
        assertFalse(controllerContent.contains("class Req"), "inner class Req should be removed from controller");

        // 应生成 @GetMapping handler 方法（因为 Req 标注了 @GetUrlQuery）
        assertTrue(controllerContent.contains("GetMapping"), "Controller should contain @GetMapping annotation");
        assertTrue(controllerContent.contains("/list-users"), "Controller should contain handler URL '/list-users'");
        assertTrue(controllerContent.contains("listUsers"), "Controller should contain method named 'listUsers'");

        // GET 请求不应该有 @RequestBody
        assertFalse(controllerContent.contains("@RequestBody"), "GET handler should not have @RequestBody");

        // 应有 query param 参数（keyword, pageNo）
        assertTrue(controllerContent.contains("keyword"), "Handler should have query param 'keyword'");
        assertTrue(controllerContent.contains("pageNo"), "Handler should have query param 'pageNo'");

        // ========== 2. 验证 Resp DTO 文件生成 ==========
        File respDtoDir = new File(basedir, "src/main/java/com/example/dto/resp");
        assertTrue(respDtoDir.exists(), "resp DTO directory should exist");

        File[] respFiles = respDtoDir.listFiles(
                (dir, name) -> name.endsWith(".java") && name.contains("ListUsers") && name.contains("Resp"));
        assertTrue(respFiles != null && respFiles.length == 1, "Should generate exactly 1 Resp DTO file for ListUsers");
        String respContent = new String(Files.readAllBytes(respFiles[0].toPath()), StandardCharsets.UTF_8);
        assertTrue(respContent.contains("userId"), "Resp DTO should contain field 'userId'");
        assertTrue(respContent.contains("username"), "Resp DTO should contain field 'username'");

        // ========== 3. @GetUrlQuery 的 Req 不应生成独立 DTO 文件 ==========
        File reqDtoDir = new File(basedir, "src/main/java/com/example/dto/req");
        if (reqDtoDir.exists()) {
            File[] reqFiles = reqDtoDir.listFiles((dir, name) -> name.endsWith(".java") && name.contains("ListUsers"));
            assertTrue(reqFiles == null || reqFiles.length == 0,
                    "GET handler with @GetUrlQuery should NOT generate a Req DTO file");
        }

        // ========== 4. 验证 Service 接口生成 ==========
        File serviceDir = new File(basedir, "src/main/java/com/example/service");
        assertTrue(serviceDir.exists(), "service directory should exist");

        File[] serviceFiles = serviceDir.listFiles((dir, name) -> name.endsWith(".java") && name.contains("ListUsers"));
        assertTrue(serviceFiles != null && serviceFiles.length == 1,
                "Should generate exactly 1 Service interface file");
    }

}

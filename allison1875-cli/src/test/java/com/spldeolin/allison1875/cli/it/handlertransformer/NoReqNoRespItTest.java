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
 * no-req-no-resp 集成测试。
 *
 * <p>验证 init 块只包含 handler+desc、无 Req/Resp 时，
 * 生成 void 返回、无参数的 PostMapping handler 和 Service。
 *
 * @author Deolin 2026-05-13
 */
public class NoReqNoRespItTest extends HandlerTransformerItBaseTest {

    @Test
    void test() throws IOException {
        runHandlerTransformer("no-req-no-resp");

        // ========== 1. 验证 Controller 被改写 ==========
        File controllerFile = new File(basedir, "src/main/java/com/example/controller/HealthController.java");
        assertTrue(controllerFile.exists(), "HealthController.java should exist");
        String controllerContent = new String(Files.readAllBytes(controllerFile.toPath()), StandardCharsets.UTF_8);

        // init 块被移除
        assertFalse(controllerContent.contains("String handler = \"/ping\""),
                "init block variable 'handler' should be removed");
        assertFalse(controllerContent.contains("String desc = \"健康检查\""),
                "init block variable 'desc' should be removed");

        // 生成 handler 方法（PostMapping 或 GetMapping）
        assertTrue(controllerContent.contains("PostMapping") || controllerContent.contains("GetMapping"),
                "Controller should contain mapping annotation");
        assertTrue(controllerContent.contains("/ping"), "Controller should contain handler URL '/ping'");
        assertTrue(controllerContent.contains("ping"), "Controller should contain method named 'ping'");

        // 无 @RequestBody（没有 Req）
        assertFalse(controllerContent.contains("@RequestBody"), "Handler without Req should not have @RequestBody");

        // 应注入 Service
        assertTrue(controllerContent.contains("Autowired") || controllerContent.contains("@Inject")
                || controllerContent.contains("@Resource"), "Controller should have injected service field");

        // ========== 2. 验证 Service 接口生成 ==========
        File serviceDir = new File(basedir, "src/main/java/com/example/service");
        assertTrue(serviceDir.exists(), "service directory should exist");

        File[] serviceFiles = serviceDir.listFiles(
                (dir, name) -> name.endsWith(".java") && name.contains("Ping") && name.contains("Service"));
        assertTrue(serviceFiles != null && serviceFiles.length == 1,
                "Should generate exactly 1 Service interface file for Ping");

        String serviceContent = new String(Files.readAllBytes(serviceFiles[0].toPath()), StandardCharsets.UTF_8);
        assertTrue(serviceContent.contains("interface"), "Service file should be an interface");
        assertTrue(serviceContent.contains("void"), "Service method should return void (no Resp defined)");
        assertTrue(serviceContent.contains("ping"), "Service should contain method 'ping'");

        // ========== 3. 验证 ServiceImpl 生成 ==========
        File serviceImplDir = new File(basedir, "src/main/java/com/example/service/impl");
        assertTrue(serviceImplDir.exists(), "service/impl directory should exist");

        File[] serviceImplFiles = serviceImplDir.listFiles(
                (dir, name) -> name.endsWith(".java") && name.contains("Ping") && name.contains("Impl"));
        assertTrue(serviceImplFiles != null && serviceImplFiles.length == 1,
                "Should generate exactly 1 ServiceImpl file for Ping");

        String serviceImplContent = new String(Files.readAllBytes(serviceImplFiles[0].toPath()),
                StandardCharsets.UTF_8);
        assertTrue(serviceImplContent.contains("class"), "ServiceImpl file should contain a class");
        assertTrue(serviceImplContent.contains("implements"), "ServiceImpl should implement the service interface");

        // ========== 4. 不应生成任何 DTO 文件 ==========
        File reqDtoDir = new File(basedir, "src/main/java/com/example/dto/req");
        if (reqDtoDir.exists()) {
            File[] reqFiles = reqDtoDir.listFiles((dir, name) -> name.endsWith(".java"));
            assertTrue(reqFiles == null || reqFiles.length == 0, "No Req DTO should be generated");
        }

        File respDtoDir = new File(basedir, "src/main/java/com/example/dto/resp");
        if (respDtoDir.exists()) {
            File[] respFiles = respDtoDir.listFiles((dir, name) -> name.endsWith(".java"));
            assertTrue(respFiles == null || respFiles.length == 0, "No Resp DTO should be generated");
        }
    }

}
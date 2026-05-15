package com.spldeolin.allison1875.cli.it;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import org.junit.jupiter.api.Test;

/**
 * basic-post 集成测试。
 *
 * <p>验证 handler-transformer 将 Controller 中的 init 块转换为 @PostMapping handler 方法，
 * 并生成 ReqDTO、RespDTO、Service 接口及 ServiceImpl 文件。
 *
 * @author Deolin 2026-05-13
 */
public class BasicPostItTest extends HandlerTransformerItBaseTest {

    @Test
    void test() throws IOException {
        runHandlerTransformer("basic-post");

        // ========== 1. 验证 Controller 被改写 ==========
        File controllerFile = new File(basedir, "src/main/java/com/example/controller/OrderController.java");
        assertTrue(controllerFile.exists(), "OrderController.java should exist");
        String controllerContent = new String(Files.readAllBytes(controllerFile.toPath()), StandardCharsets.UTF_8);

        // init 块应该被移除
        assertFalse(controllerContent.contains("String handler = \"/create-order\""),
                "init block variable 'handler' should be removed");
        assertFalse(controllerContent.contains("String desc = \"创建订单\""),
                "init block variable 'desc' should be removed");
        assertFalse(controllerContent.contains("class Req"), "inner class Req should be removed from controller");
        assertFalse(controllerContent.contains("class Resp"), "inner class Resp should be removed from controller");

        // 应生成 @PostMapping handler 方法
        assertTrue(controllerContent.contains("PostMapping"), "Controller should contain @PostMapping annotation");
        assertTrue(controllerContent.contains("/create-order"),
                "Controller should contain handler URL '/create-order'");
        assertTrue(controllerContent.contains("createOrder"), "Controller should contain method named 'createOrder'");

        // 应注入 Service
        assertTrue(controllerContent.contains("Autowired") || controllerContent.contains("@Inject")
                || controllerContent.contains("@Resource"), "Controller should have injected service field");

        // ========== 2. 验证 Req DTO 文件生成 ==========
        File reqDtoDir = new File(basedir, "src/main/java/com/example/dto/req");
        assertTrue(reqDtoDir.exists(), "req DTO directory should exist");

        File[] reqFiles = reqDtoDir.listFiles(
                (dir, name) -> name.endsWith(".java") && name.contains("CreateOrder") && name.contains("Req"));
        assertTrue(reqFiles != null && reqFiles.length == 1, "Should generate exactly 1 Req DTO file for CreateOrder");

        String reqContent = new String(Files.readAllBytes(reqFiles[0].toPath()), StandardCharsets.UTF_8);
        assertTrue(reqContent.contains("orderName"), "Req DTO should contain field 'orderName'");
        assertTrue(reqContent.contains("amount"), "Req DTO should contain field 'amount'");
        assertTrue(reqContent.contains("NotBlank"), "Req DTO should preserve @NotBlank annotation");
        assertTrue(reqContent.contains("NotNull"), "Req DTO should preserve @NotNull annotation");

        // ========== 3. 验证 Resp DTO 文件生成 ==========
        File respDtoDir = new File(basedir, "src/main/java/com/example/dto/resp");
        assertTrue(respDtoDir.exists(), "resp DTO directory should exist");

        File[] respFiles = respDtoDir.listFiles(
                (dir, name) -> name.endsWith(".java") && name.contains("CreateOrder") && name.contains("Resp"));
        assertTrue(respFiles != null && respFiles.length == 1,
                "Should generate exactly 1 Resp DTO file for CreateOrder");

        String respContent = new String(Files.readAllBytes(respFiles[0].toPath()), StandardCharsets.UTF_8);
        assertTrue(respContent.contains("orderId"), "Resp DTO should contain field 'orderId'");
        assertTrue(respContent.contains("status"), "Resp DTO should contain field 'status'");

        // ========== 4. 验证 Service 接口生成 ==========
        File serviceDir = new File(basedir, "src/main/java/com/example/service");
        assertTrue(serviceDir.exists(), "service directory should exist");

        File[] serviceFiles = serviceDir.listFiles(
                (dir, name) -> name.endsWith(".java") && name.contains("CreateOrder") && name.contains("Service"));
        assertTrue(serviceFiles != null && serviceFiles.length == 1,
                "Should generate exactly 1 Service interface file");

        String serviceContent = new String(Files.readAllBytes(serviceFiles[0].toPath()), StandardCharsets.UTF_8);
        assertTrue(serviceContent.contains("interface"), "Service file should be an interface");
        assertTrue(serviceContent.contains("createOrder"), "Service should contain method 'createOrder'");

        // ========== 5. 验证 ServiceImpl 生成 ==========
        File serviceImplDir = new File(basedir, "src/main/java/com/example/service/impl");
        assertTrue(serviceImplDir.exists(), "service/impl directory should exist");

        File[] serviceImplFiles = serviceImplDir.listFiles(
                (dir, name) -> name.endsWith(".java") && name.contains("CreateOrder") && name.contains("Impl"));
        assertTrue(serviceImplFiles != null && serviceImplFiles.length == 1,
                "Should generate exactly 1 ServiceImpl file");

        String serviceImplContent = new String(Files.readAllBytes(serviceImplFiles[0].toPath()),
                StandardCharsets.UTF_8);
        assertTrue(serviceImplContent.contains("class"), "ServiceImpl file should contain a class");
        assertTrue(serviceImplContent.contains("createOrder"), "ServiceImpl should contain method 'createOrder'");
        assertTrue(serviceImplContent.contains("implements"), "ServiceImpl should implement the service interface");
    }

}

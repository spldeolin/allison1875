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
 * nested-dto 集成测试。
 *
 * 验证 Req/Resp 内嵌套子类时，子类被提取为独立 DTO 文件，
 * 父类中替换为字段引用，Req 侧嵌套字段带 @Valid。
 *
 * @author Deolin 2026-05-13
 */
public class NestedDtoItTest extends HandlerTransformerItBaseTest {

    @Test
    void test() throws IOException {
        runHandlerTransformer("nested-dto");

        // ========== 1. 验证 Controller 被改写 ==========
        File controllerFile = new File(basedir, "src/main/java/com/example/controller/ShippingController.java");
        assertTrue(controllerFile.exists(), "ShippingController.java should exist");
        String controllerContent = new String(Files.readAllBytes(controllerFile.toPath()), StandardCharsets.UTF_8);

        // init 块移除
        assertFalse(controllerContent.contains("class Req"), "inner class Req should be removed");
        assertFalse(controllerContent.contains("class Resp"), "inner class Resp should be removed");
        assertFalse(controllerContent.contains("class Address"), "nested class Address should be removed");
        assertFalse(controllerContent.contains("class Logistics"), "nested class Logistics should be removed");

        // handler 生成
        assertTrue(controllerContent.contains("/create-shipping"), "Should contain URL");
        assertTrue(controllerContent.contains("createShipping"), "Should contain method name");

        // ========== 2. 验证嵌套 DTO 独立文件生成 ==========
        File reqDtoDir = new File(basedir, "src/main/java/com/example/dto/req");
        assertTrue(reqDtoDir.exists(), "req DTO directory should exist");

        // Address 嵌套类应生成独立 DTO
        File[] addressFiles = reqDtoDir.listFiles((dir, name) -> name.endsWith(".java") && name.contains("Address"));
        assertTrue(addressFiles != null && addressFiles.length == 1, "Should generate AddressDTO file in req package");

        String addressContent = new String(Files.readAllBytes(addressFiles[0].toPath()), StandardCharsets.UTF_8);
        assertTrue(addressContent.contains("province"), "AddressDTO should contain 'province'");
        assertTrue(addressContent.contains("city"), "AddressDTO should contain 'city'");
        assertTrue(addressContent.contains("detail"), "AddressDTO should contain 'detail'");

        // Req 主文件
        File[] reqFiles = reqDtoDir.listFiles(
                (dir, name) -> name.endsWith(".java") && name.contains("CreateShipping") && name.contains("Req"));
        assertTrue(reqFiles != null && reqFiles.length == 1, "Should generate CreateShippingReq DTO");

        String reqContent = new String(Files.readAllBytes(reqFiles[0].toPath()), StandardCharsets.UTF_8);
        assertTrue(reqContent.contains("recipientName"), "Req should contain 'recipientName'");
        // 嵌套类应被替换为字段引用
        assertTrue(reqContent.contains("address") || reqContent.contains("Address"),
                "Req should reference Address as a field");
        // Req 侧嵌套字段应有 @Valid
        assertTrue(reqContent.contains("Valid"), "Nested DTO field in Req should have @Valid annotation");

        // ========== 3. 验证 Resp 嵌套 DTO ==========
        File respDtoDir = new File(basedir, "src/main/java/com/example/dto/resp");
        assertTrue(respDtoDir.exists(), "resp DTO directory should exist");

        File[] logisticsFiles = respDtoDir.listFiles(
                (dir, name) -> name.endsWith(".java") && name.contains("Logistics"));
        assertTrue(logisticsFiles != null && logisticsFiles.length == 1,
                "Should generate LogisticsDTO file in resp package");

        String logisticsContent = new String(Files.readAllBytes(logisticsFiles[0].toPath()), StandardCharsets.UTF_8);
        assertTrue(logisticsContent.contains("company"), "LogisticsDTO should contain 'company'");
        assertTrue(logisticsContent.contains("trackingNo"), "LogisticsDTO should contain 'trackingNo'");
    }

}
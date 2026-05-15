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
 * multiple-init-decs 集成测试。
 *
 * 验证同一个 Controller 包含多个 init 块时，每个都被独立转换。
 *
 * @author Deolin 2026-05-13
 */
public class MultipleInitDecsItTest extends HandlerTransformerItBaseTest {

    @Test
    void test() throws IOException {
        runHandlerTransformer("multiple-init-decs");

        // ========== 1. 验证 Controller 被改写 ==========
        File controllerFile = new File(basedir, "src/main/java/com/example/controller/ProductController.java");
        assertTrue(controllerFile.exists(), "ProductController.java should exist");
        String controllerContent = new String(Files.readAllBytes(controllerFile.toPath()), StandardCharsets.UTF_8);

        // 所有 init 块应被移除
        assertFalse(controllerContent.contains("String handler ="),
                "All init block handler declarations should be removed");
        assertFalse(controllerContent.contains("String desc ="), "All init block desc declarations should be removed");

        // 3 个 handler 方法应该全部生成
        assertTrue(controllerContent.contains("/create-product"), "Should contain URL '/create-product'");
        assertTrue(controllerContent.contains("/delete-product"), "Should contain URL '/delete-product'");
        assertTrue(controllerContent.contains("/get-product-detail"), "Should contain URL '/get-product-detail'");
        assertTrue(controllerContent.contains("createProduct"), "Should contain method 'createProduct'");
        assertTrue(controllerContent.contains("deleteProduct"), "Should contain method 'deleteProduct'");
        assertTrue(controllerContent.contains("getProductDetail"), "Should contain method 'getProductDetail'");

        // ========== 2. 验证 Service 目录（非 oneService 模式下每个 init 生成独立 Service） ==========
        File serviceDir = new File(basedir, "src/main/java/com/example/service");
        assertTrue(serviceDir.exists(), "service directory should exist");
        File[] serviceFiles = serviceDir.listFiles((dir, name) -> name.endsWith(".java"));
        assertTrue(serviceFiles != null && serviceFiles.length >= 3,
                "Should generate at least 3 Service interface files, found: " + (serviceFiles == null ? "null"
                        : serviceFiles.length));

        // ========== 3. 验证 ServiceImpl 目录 ==========
        File serviceImplDir = new File(basedir, "src/main/java/com/example/service/impl");
        assertTrue(serviceImplDir.exists(), "service/impl directory should exist");
        File[] serviceImplFiles = serviceImplDir.listFiles((dir, name) -> name.endsWith(".java"));
        assertTrue(serviceImplFiles != null && serviceImplFiles.length >= 3,
                "Should generate at least 3 ServiceImpl files, found: " + (serviceImplFiles == null ? "null"
                        : serviceImplFiles.length));

        // ========== 4. 验证 Req DTO ==========
        File reqDtoDir = new File(basedir, "src/main/java/com/example/dto/req");
        assertTrue(reqDtoDir.exists(), "req DTO directory should exist");
        File[] reqFiles = reqDtoDir.listFiles((dir, name) -> name.endsWith(".java"));
        assertTrue(reqFiles != null && reqFiles.length >= 2,
                "Should generate at least 2 Req DTO files, found: " + (reqFiles == null ? "null" : reqFiles.length));

        // ========== 5. 验证 Resp DTO ==========
        File respDtoDir = new File(basedir, "src/main/java/com/example/dto/resp");
        assertTrue(respDtoDir.exists(), "resp DTO directory should exist");
        File[] respFiles = respDtoDir.listFiles((dir, name) -> name.endsWith(".java"));
        assertTrue(respFiles != null && respFiles.length >= 2,
                "Should generate at least 2 Resp DTO files, found: " + (respFiles == null ? "null" : respFiles.length));
    }

}
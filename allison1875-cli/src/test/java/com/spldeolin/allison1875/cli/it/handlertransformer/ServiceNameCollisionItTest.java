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
 * service-name-collision 集成测试。
 *
 * <p>验证非 enableOneService 模式下，当目标 Service 文件名已存在时，antiDuplication 触发 rename（追加 Ex 后缀），
 * 预置文件保持不变。
 *
 * @author Deolin 2026-05-15
 */
public class ServiceNameCollisionItTest extends HandlerTransformerItBaseTest {

    @Test
    void test() throws IOException {
        runHandlerTransformer("service-name-collision");

        // ========== 1. 验证 Controller 被改写 ==========
        File controllerFile = new File(basedir, "src/main/java/com/example/controller/OrderController.java");
        assertTrue(controllerFile.exists(), "OrderController.java should exist");
        String controllerContent = new String(Files.readAllBytes(controllerFile.toPath()), StandardCharsets.UTF_8);

        assertFalse(controllerContent.contains("String handler ="), "init block should be removed");
        assertTrue(controllerContent.contains("/create-order"), "Should contain URL '/create-order'");
        assertTrue(controllerContent.contains("createOrder"), "Should contain method 'createOrder'");

        // ========== 2. 验证预置的 CreateOrderService.java 保持不变 ==========
        File preExistingService = new File(basedir, "src/main/java/com/example/service/CreateOrderService.java");
        assertTrue(preExistingService.exists(), "Pre-existing CreateOrderService.java should still exist");
        String preExistingContent = new String(Files.readAllBytes(preExistingService.toPath()), StandardCharsets.UTF_8);
        assertTrue(preExistingContent.contains("queryOrder"), "Pre-existing Service should still contain 'queryOrder'");

        // ========== 3. 验证生成了重命名后的 Service 文件（CreateOrderServiceEx.java） ==========
        File serviceDir = new File(basedir, "src/main/java/com/example/service");
        assertTrue(serviceDir.exists(), "service directory should exist");

        // 应该至少有两个 java 文件：原 CreateOrderService 和新生成的 CreateOrderServiceEx
        File[] serviceFiles = serviceDir.listFiles((dir, name) -> name.endsWith(".java"));
        assertTrue(serviceFiles != null && serviceFiles.length >= 2,
                "Should have at least 2 Service files (original + renamed), found: " + (serviceFiles == null ? "null"
                        : serviceFiles.length));

        // 验证存在 CreateOrderServiceEx.java
        File renamedService = new File(basedir, "src/main/java/com/example/service/CreateOrderServiceEx.java");
        assertTrue(renamedService.exists(), "Renamed Service 'CreateOrderServiceEx.java' should exist");

        String renamedContent = new String(Files.readAllBytes(renamedService.toPath()), StandardCharsets.UTF_8);
        assertTrue(renamedContent.contains("createOrder"), "Renamed Service should contain method 'createOrder'");

        // ========== 4. 验证生成了 ServiceImpl ==========
        File serviceImplDir = new File(basedir, "src/main/java/com/example/service/impl");
        assertTrue(serviceImplDir.exists(), "service/impl directory should exist");

        // ServiceImpl 应随着 Service 重命名生成（不带Ex后缀的路径未冲突）
        File[] serviceImplFiles = serviceImplDir.listFiles((dir, name) -> name.endsWith(".java"));
        assertTrue(serviceImplFiles != null && serviceImplFiles.length >= 1,
                "Should have at least 1 ServiceImpl file, found: " + (serviceImplFiles == null ? "null"
                        : serviceImplFiles.length));

        // 验证 ServiceImpl 内容包含 createOrder 方法
        String implContent = new String(Files.readAllBytes(serviceImplFiles[0].toPath()), StandardCharsets.UTF_8);
        assertTrue(implContent.contains("createOrder"), "ServiceImpl should contain method 'createOrder'");
    }

}

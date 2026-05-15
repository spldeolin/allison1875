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
 * service-method-name-dedup 集成测试。
 *
 * <p>验证 enableOneService=true + 预置 Service 中已有 createOrder 方法时，init 块生成的方法名
 * 被 antiDuplication 自动重命名（追加 Ex 后缀）。
 *
 * @author Deolin 2026-05-15
 */
public class ServiceMethodNameDeduplicationItTest extends HandlerTransformerItBaseTest {

    @Test
    void test() throws IOException {
        runHandlerTransformer("service-method-name-dedup");

        // ========== 1. 验证 Controller 被改写 ==========
        File controllerFile = new File(basedir, "src/main/java/com/example/controller/OrderController.java");
        assertTrue(controllerFile.exists(), "OrderController.java should exist");
        String controllerContent = new String(Files.readAllBytes(controllerFile.toPath()), StandardCharsets.UTF_8);

        assertFalse(controllerContent.contains("String handler ="), "init block should be removed");
        assertTrue(controllerContent.contains("/create-order"), "Should contain URL '/create-order'");

        // ========== 2. 验证 Service 接口包含原名 + 去重后的方法名 ==========
        File serviceFile = new File(basedir, "src/main/java/com/example/service/OrderService.java");
        assertTrue(serviceFile.exists(), "OrderService.java should exist");
        String serviceContent = new String(Files.readAllBytes(serviceFile.toPath()), StandardCharsets.UTF_8);

        // 已有的 createOrder 保留
        assertTrue(serviceContent.contains("createOrder"), "Service should contain method 'createOrder'");
        // 新方法应被去重重命名：createOrder → createOrderEx
        assertTrue(serviceContent.contains("createOrderEx"),
                "Service should contain de-duplicated method 'createOrderEx'");

        // ========== 3. 验证 ServiceImpl 同样包含去重后的方法名 ==========
        File serviceImplFile = new File(basedir, "src/main/java/com/example/service/impl/OrderServiceImpl.java");
        assertTrue(serviceImplFile.exists(), "OrderServiceImpl.java should exist");
        String serviceImplContent = new String(Files.readAllBytes(serviceImplFile.toPath()), StandardCharsets.UTF_8);

        assertTrue(serviceImplContent.contains("createOrder"), "ServiceImpl should contain method 'createOrder'");
        assertTrue(serviceImplContent.contains("createOrderEx"),
                "ServiceImpl should contain de-duplicated method 'createOrderEx'");

        // ========== 4. Controller 中调用的方法名应是重命名后的 ==========
        assertTrue(controllerContent.contains("createOrderEx"),
                "Controller should call de-duplicated method 'createOrderEx'");
    }

}

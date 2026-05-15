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
 * enable-one-service 集成测试。
 *
 * 验证 enableOneService=true 时，同一 Controller 的多个 init 块共享一个 Service 接口+Impl，
 * 多个方法聚合在同一个文件中。
 *
 * @author Deolin 2026-05-13
 */
public class EnableOneServiceItTest extends HandlerTransformerItBaseTest {

    @Test
    void test() throws IOException {
        runHandlerTransformer("enable-one-service");

        // ========== 1. 验证 Controller 被改写 ==========
        File controllerFile = new File(basedir, "src/main/java/com/example/controller/AccountController.java");
        assertTrue(controllerFile.exists(), "AccountController.java should exist");
        String controllerContent = new String(Files.readAllBytes(controllerFile.toPath()), StandardCharsets.UTF_8);

        // 所有 init 块被替换
        assertFalse(controllerContent.contains("String handler ="), "All init blocks should be removed");
        assertTrue(controllerContent.contains("/create-account"), "Should contain URL '/create-account'");
        assertTrue(controllerContent.contains("/freeze-account"), "Should contain URL '/freeze-account'");
        assertTrue(controllerContent.contains("/get-account-balance"), "Should contain URL '/get-account-balance'");

        // ========== 2. 验证 oneService 模式：只生成 1 个 Service 接口 ==========
        File serviceDir = new File(basedir, "src/main/java/com/example/service");
        assertTrue(serviceDir.exists(), "service directory should exist");

        File[] serviceFiles = serviceDir.listFiles((dir, name) -> name.endsWith(".java"));
        assertTrue(serviceFiles != null && serviceFiles.length == 1,
                "enableOneService=true should generate exactly 1 Service interface, found: " + (serviceFiles == null
                        ? "null" : serviceFiles.length));
        assertTrue(serviceFiles[0].getName().contains("AccountService"),
                "Service name should be 'AccountService' (derived from AccountController), found: "
                        + serviceFiles[0].getName());

        // 该 Service 接口应包含 3 个方法
        String serviceContent = new String(Files.readAllBytes(serviceFiles[0].toPath()), StandardCharsets.UTF_8);
        assertTrue(serviceContent.contains("createAccount"), "Service should contain 'createAccount'");
        assertTrue(serviceContent.contains("freezeAccount"), "Service should contain 'freezeAccount'");
        assertTrue(serviceContent.contains("getAccountBalance"), "Service should contain 'getAccountBalance'");

        // ========== 3. 只应生成 1 个 ServiceImpl ==========
        File serviceImplDir = new File(basedir, "src/main/java/com/example/service/impl");
        assertTrue(serviceImplDir.exists(), "service/impl directory should exist");

        File[] serviceImplFiles = serviceImplDir.listFiles((dir, name) -> name.endsWith(".java"));
        assertTrue(serviceImplFiles != null && serviceImplFiles.length == 1,
                "enableOneService=true should generate exactly 1 ServiceImpl, found: " + (serviceImplFiles == null
                        ? "null" : serviceImplFiles.length));

        String serviceImplContent = new String(Files.readAllBytes(serviceImplFiles[0].toPath()),
                StandardCharsets.UTF_8);
        assertTrue(serviceImplContent.contains("createAccount"), "ServiceImpl should contain 'createAccount'");
        assertTrue(serviceImplContent.contains("freezeAccount"), "ServiceImpl should contain 'freezeAccount'");
        assertTrue(serviceImplContent.contains("getAccountBalance"), "ServiceImpl should contain 'getAccountBalance'");
    }

}
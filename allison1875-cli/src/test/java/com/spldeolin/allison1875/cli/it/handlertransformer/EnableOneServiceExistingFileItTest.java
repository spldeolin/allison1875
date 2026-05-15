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
 * enable-one-service-existing-file 集成测试。
 *
 * <p>验证 enableOneService=true 且 Service/ServiceImpl 文件已存在时，工具复用已有文件而非重新生成，
 * 并将新方法追加到已有方法之后。
 *
 * @author Deolin 2026-05-15
 */
public class EnableOneServiceExistingFileItTest extends HandlerTransformerItBaseTest {

    @Test
    void test() throws IOException {
        runHandlerTransformer("enable-one-service-existing-file");

        // ========== 1. 验证 Controller 被改写 ==========
        File controllerFile = new File(basedir, "src/main/java/com/example/controller/AccountController.java");
        assertTrue(controllerFile.exists(), "AccountController.java should exist");
        String controllerContent = new String(Files.readAllBytes(controllerFile.toPath()), StandardCharsets.UTF_8);

        assertFalse(controllerContent.contains("String handler ="), "All init blocks should be removed");
        assertTrue(controllerContent.contains("/create-account"), "Should contain URL '/create-account'");
        assertTrue(controllerContent.contains("/freeze-account"), "Should contain URL '/freeze-account'");

        // ========== 2. 验证 Service 接口复用已有文件，包含已有方法 + 新方法 ==========
        File serviceDir = new File(basedir, "src/main/java/com/example/service");
        assertTrue(serviceDir.exists(), "service directory should exist");

        File[] serviceFiles = serviceDir.listFiles((dir, name) -> name.endsWith(".java"));
        assertTrue(serviceFiles != null && serviceFiles.length == 1,
                "Should still have exactly 1 Service file (reused), found: " + (serviceFiles == null ? "null"
                        : serviceFiles.length));
        assertTrue(serviceFiles[0].getName().contains("AccountService"), "Service name should be 'AccountService'");

        String serviceContent = new String(Files.readAllBytes(serviceFiles[0].toPath()), StandardCharsets.UTF_8);
        // 已有的 cancelAccount 方法应保留
        assertTrue(serviceContent.contains("cancelAccount"), "Should still contain pre-existing 'cancelAccount'");
        // 新追加的 2 个方法
        assertTrue(serviceContent.contains("createAccount"), "Service should contain newly added 'createAccount'");
        assertTrue(serviceContent.contains("freezeAccount"), "Service should contain newly added 'freezeAccount'");

        // ========== 3. 验证 ServiceImpl 复用已有文件 ==========
        File serviceImplDir = new File(basedir, "src/main/java/com/example/service/impl");
        assertTrue(serviceImplDir.exists(), "service/impl directory should exist");

        File[] serviceImplFiles = serviceImplDir.listFiles((dir, name) -> name.endsWith(".java"));
        assertTrue(serviceImplFiles != null && serviceImplFiles.length == 1,
                "Should still have exactly 1 ServiceImpl file (reused), found: " + (serviceImplFiles == null ? "null"
                        : serviceImplFiles.length));

        String serviceImplContent = new String(Files.readAllBytes(serviceImplFiles[0].toPath()),
                StandardCharsets.UTF_8);
        // 已有的 cancelAccount 方法应保留
        assertTrue(serviceImplContent.contains("cancelAccount"),
                "ServiceImpl should still contain pre-existing 'cancelAccount'");
        // 新追加的方法
        assertTrue(serviceImplContent.contains("createAccount"),
                "ServiceImpl should contain newly added 'createAccount'");
        assertTrue(serviceImplContent.contains("freezeAccount"),
                "ServiceImpl should contain newly added 'freezeAccount'");
    }

}

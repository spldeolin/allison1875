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
 * controller-annotation 集成测试。
 *
 * 验证使用 @Controller（而非 @RestController）标注的类也能被检测并转换。
 *
 * @author Deolin 2026-05-13
 */
public class ControllerAnnotationItTest extends HandlerTransformerItBaseTest {

    @Test
    void test() throws IOException {
        runHandlerTransformer("controller-annotation");

        // ========== 1. 验证 Controller 被改写 ==========
        File controllerFile = new File(basedir, "src/main/java/com/example/controller/LegacyController.java");
        assertTrue(controllerFile.exists(), "LegacyController.java should exist");
        String controllerContent = new String(Files.readAllBytes(controllerFile.toPath()), StandardCharsets.UTF_8);

        // init 块移除
        assertFalse(controllerContent.contains("String handler = \"/do-legacy\""), "init block should be removed");
        assertFalse(controllerContent.contains("class Req"), "inner class Req should be removed");

        // handler 方法生成
        assertTrue(controllerContent.contains("/do-legacy"), "Should contain URL '/do-legacy'");
        assertTrue(controllerContent.contains("doLegacy"), "Should contain method 'doLegacy'");
        assertTrue(controllerContent.contains("PostMapping"), "Should contain @PostMapping");

        // @Controller 注解保留
        assertTrue(controllerContent.contains("@Controller"), "@Controller annotation should be preserved");

        // ========== 2. 验证 Service 正常生成 ==========
        File serviceDir = new File(basedir, "src/main/java/com/example/service");
        assertTrue(serviceDir.exists(), "service directory should exist");
        File[] serviceFiles = serviceDir.listFiles((dir, name) -> name.endsWith(".java") && name.contains("DoLegacy"));
        assertTrue(serviceFiles != null && serviceFiles.length == 1, "Should generate 1 Service file");

        // ========== 3. 验证 DTO 正常生成 ==========
        File reqDtoDir = new File(basedir, "src/main/java/com/example/dto/req");
        assertTrue(reqDtoDir.exists(), "req DTO directory should exist");
        File[] reqFiles = reqDtoDir.listFiles((dir, name) -> name.endsWith(".java") && name.contains("DoLegacy"));
        assertTrue(reqFiles != null && reqFiles.length == 1, "Should generate 1 Req DTO");
    }

}
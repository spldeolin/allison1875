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
 * page-annotation 集成测试。
 *
 * 验证 Resp 标注 @P 注解时，handler 返回类型被包装为 PageResult&lt;XxxResp&gt;。
 *
 * @author Deolin 2026-05-13
 */
public class PageAnnotationItTest extends HandlerTransformerItBaseTest {

    @Test
    void test() throws IOException {
        runHandlerTransformer("page-annotation");

        // ========== 1. 验证 Controller 被改写 ==========
        File controllerFile = new File(basedir, "src/main/java/com/example/controller/ArticleController.java");
        assertTrue(controllerFile.exists(), "ArticleController.java should exist");
        String controllerContent = new String(Files.readAllBytes(controllerFile.toPath()), StandardCharsets.UTF_8);

        // init 块移除
        assertFalse(controllerContent.contains("String handler ="), "init block should be removed");

        // handler 方法生成
        assertTrue(controllerContent.contains("/page-articles"), "Should contain URL '/page-articles'");
        assertTrue(controllerContent.contains("pageArticles"), "Should contain method 'pageArticles'");

        // 返回类型应包含 PageResult（由 @P 注解触发）
        assertTrue(controllerContent.contains("PageResult"),
                "Handler return type should contain 'PageResult' due to @P annotation");

        // ========== 2. 验证 Service 方法返回类型也应包含 PageResult ==========
        File serviceDir = new File(basedir, "src/main/java/com/example/service");
        assertTrue(serviceDir.exists(), "service directory should exist");
        File[] serviceFiles = serviceDir.listFiles(
                (dir, name) -> name.endsWith(".java") && name.contains("PageArticles"));
        assertTrue(serviceFiles != null && serviceFiles.length == 1, "Should generate 1 Service file");
        String serviceContent = new String(Files.readAllBytes(serviceFiles[0].toPath()), StandardCharsets.UTF_8);
        assertTrue(serviceContent.contains("PageResult"), "Service method return type should contain 'PageResult'");

        // ========== 3. 验证 Resp DTO 生成 ==========
        File respDtoDir = new File(basedir, "src/main/java/com/example/dto/resp");
        assertTrue(respDtoDir.exists(), "resp DTO directory should exist");
        File[] respFiles = respDtoDir.listFiles(
                (dir, name) -> name.endsWith(".java") && name.contains("PageArticles") && name.contains("Resp"));
        assertTrue(respFiles != null && respFiles.length == 1, "Should generate 1 Resp DTO file");

        String respContent = new String(Files.readAllBytes(respFiles[0].toPath()), StandardCharsets.UTF_8);
        assertTrue(respContent.contains("articleId"), "Resp DTO should contain 'articleId'");
        assertTrue(respContent.contains("title"), "Resp DTO should contain 'title'");
    }

}
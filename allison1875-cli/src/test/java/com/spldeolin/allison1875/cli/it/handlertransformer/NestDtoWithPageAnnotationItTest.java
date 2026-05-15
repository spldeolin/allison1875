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
 * nest-dto-with-page-annotation 集成测试。
 *
 * <p>验证嵌套 DTO 标注 @P 注解时，父 DTO 中字段类型为 PageResult&lt;XxxDTO&gt;，字段名被复数化。
 *
 * @author Deolin 2026-05-15
 */
public class NestDtoWithPageAnnotationItTest extends HandlerTransformerItBaseTest {

    @Test
    void test() throws IOException {
        runHandlerTransformer("nest-dto-with-page-annotation");

        // ========== 1. 验证 Controller 被改写 ==========
        File controllerFile = new File(basedir, "src/main/java/com/example/controller/BookController.java");
        assertTrue(controllerFile.exists(), "BookController.java should exist");
        String controllerContent = new String(Files.readAllBytes(controllerFile.toPath()), StandardCharsets.UTF_8);

        assertFalse(controllerContent.contains("class Resp"), "inner class Resp should be removed");
        assertFalse(controllerContent.contains("class Chapter"), "nested class Chapter should be removed");
        assertTrue(controllerContent.contains("/get-book"), "Should contain URL");

        // ========== 2. 验证嵌套 Chapter DTO 生成为独立文件 ==========
        File respDtoDir = new File(basedir, "src/main/java/com/example/dto/resp");
        assertTrue(respDtoDir.exists(), "resp DTO directory should exist");

        File[] chapterFiles = respDtoDir.listFiles((dir, name) -> name.endsWith(".java") && name.contains("Chapter"));
        assertTrue(chapterFiles != null && chapterFiles.length == 1, "Should generate ChapterDTO file");

        // ========== 3. 验证 Resp 主 DTO 中字段名为复数、类型为 PageResult ==========
        File[] respFiles = respDtoDir.listFiles(
                (dir, name) -> name.endsWith(".java") && name.contains("GetBook") && name.contains("Resp"));
        assertTrue(respFiles != null && respFiles.length == 1, "Should generate GetBookResp DTO");

        String respContent = new String(Files.readAllBytes(respFiles[0].toPath()), StandardCharsets.UTF_8);
        assertTrue(respContent.contains("bookName"), "Resp DTO should contain 'bookName'");
        // 字段名应为复数（chapter → chapters）
        assertTrue(respContent.contains("chapters"),
                "Resp DTO should contain plural field name 'chapters' for @P Chapter");
        // 字段类型应为 PageResult
        assertTrue(respContent.contains("PageResult"),
                "Resp DTO should contain PageResult type for @P nested DTO field");
    }

}

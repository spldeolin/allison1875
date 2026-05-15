package com.spldeolin.allison1875.cli.it.docanalyzer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * top-level-simple-value-body 集成测试。
 *
 * <p>验证 @RequestBody String/Integer 和直接返回 String/Integer 时，
 * Request/Response Body 渲染为简单值类型名称（如 String、Integer），不含字段表格。
 *
 * @author Deolin 2026-05-15
 */
public class TopLevelSimpleValueBodyItTest extends DocAnalyzerItBaseTest {

    @Test
    void test() throws IOException {
        runDocAnalyzer("top-level-simple-value-body");

        File apiDocsDir = new File(basedir, "api-docs");
        assertTrue(apiDocsDir.exists(), "api-docs directory should exist");

        List<File> mdFiles = new ArrayList<>();
        Files.walkFileTree(apiDocsDir.toPath(), new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                if (file.toString().endsWith(".md")) {
                    mdFiles.add(file.toFile());
                }
                return FileVisitResult.CONTINUE;
            }
        });
        assertTrue(mdFiles.size() > 0, "At least one .md file should be generated");

        StringBuilder sb = new StringBuilder();
        for (File mdFile : mdFiles) {
            sb.append(new String(Files.readAllBytes(mdFile.toPath()), StandardCharsets.UTF_8));
            sb.append("\n");
        }
        String allContent = sb.toString();

        // 验证 md 文件名
        assertEquals(1, mdFiles.size(), "Should generate exactly 1 .md file");
        assertEquals("简单值处理.md", mdFiles.get(0).getName(), "Markdown filename should be '简单值处理.md'");

        // 验证 handlers
        assertTrue(allContent.contains("发送字符串消息"), "Should contain '发送字符串消息'");
        assertTrue(allContent.contains("POST /api/simple/string"), "Should contain 'POST /api/simple/string'");
        assertTrue(allContent.contains("发送数字计数"), "Should contain '发送数字计数'");
        assertTrue(allContent.contains("POST /api/simple/integer"), "Should contain 'POST /api/simple/integer'");

        // 验证 String 类型在 Request/Response Body 中渲染
        assertTrue(allContent.contains("String"), "Should contain type 'String'");

        // 验证 Integer 类型在 Request/Response Body 中渲染
        assertTrue(allContent.contains("Integer"), "Should contain type 'Integer'");

        // 验证 @return 描述
        assertTrue(allContent.contains("处理结果"), "Should contain @return '处理结果'");

        // 验证 @param 描述
        assertTrue(allContent.contains("消息内容"), "Should contain @param '消息内容'");

        // 验证 Markdown 结构
        assertTrue(allContent.contains("### Request Body (application/json)"), "Should contain request body section");
        assertTrue(allContent.contains("### Response Body (application/json)"), "Should contain response body section");
    }

}

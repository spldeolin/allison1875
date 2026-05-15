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
 * java-record 集成测试。
 *
 * <p>验证 doc-analyzer 对 Java Record 类型的 DTO（请求体和响应体）的文档化输出，
 * 包括 record component 的字段名和 Javadoc @param 注释。
 *
 * @author Deolin 2026-05-13
 */
public class JavaRecordItTest extends DocAnalyzerItBaseTest {

    @Test
    void test() throws IOException {
        runDocAnalyzer("java-record");

        File apiDocsDir = new File(basedir, "api-docs");
        assertTrue(apiDocsDir.exists(), "api-docs directory should exist");

        // 查找所有 md 文件
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

        // 合并所有 md 内容
        StringBuilder sb = new StringBuilder();
        for (File mdFile : mdFiles) {
            sb.append(new String(Files.readAllBytes(mdFile.toPath()), StandardCharsets.UTF_8));
            sb.append("\n");
        }
        String allContent = sb.toString();

        // 验证 md 文件名和数量
        assertEquals(1, mdFiles.size(), "Should generate exactly 1 .md file, but found " + mdFiles.size());
        assertEquals("地址管理.md", mdFiles.get(0).getName(), "Markdown filename should be '地址管理.md'");

        // Endpoint detected
        assertTrue(allContent.contains("创建地址"), "Should contain handler description");
        assertTrue(allContent.contains("POST /api/addresses"), "Should contain 'POST /api/addresses'");

        // Record components from request body should appear as fields with Javadoc @param comments
        assertTrue(allContent.contains("title"), "Should contain record component 'title'");
        assertTrue(allContent.contains("地址标题"), "Should contain record component comment '地址标题'");
        assertTrue(allContent.contains("city"), "Should contain record component 'city'");
        assertTrue(allContent.contains("城市"), "Should contain record component comment '城市'");
        assertTrue(allContent.contains("zipCode"), "Should contain record component 'zipCode'");
        assertTrue(allContent.contains("邮编"), "Should contain record component comment '邮编'");

        // Response record components
        assertTrue(allContent.contains("id"), "Should contain response record component 'id'");
        assertTrue(allContent.contains("ID"), "Should contain response field comment 'ID'");

        // 验证 Markdown 结构
        assertTrue(allContent.contains("### Request Body (application/json)"), "Should contain Request Body section");
        assertTrue(allContent.contains("### Response Body (application/json)"), "Should contain Response Body section");
    }

}
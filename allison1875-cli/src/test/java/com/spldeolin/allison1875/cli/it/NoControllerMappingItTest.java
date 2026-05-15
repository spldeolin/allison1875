package com.spldeolin.allison1875.cli.it;

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
 * no-controller-mapping 集成测试。
 *
 * 验证 doc-analyzer 能正确处理没有类级 @RequestMapping 的 Controller，
 * URL 直接使用 method 级路径。
 *
 * @author Deolin 2025-05-13
 */
public class NoControllerMappingItTest extends DocAnalyzerItBaseTest {

    @Test
    void test() throws IOException {
        runDocAnalyzer("no-controller-mapping");

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
        assertEquals("无类级RequestMapping.md", mdFiles.get(0).getName(),
                "Markdown filename should be '无类级RequestMapping.md'");

        assertTrue(allContent.contains("Ping接口"), "Should contain 'Ping接口'");
        assertTrue(allContent.contains("Version接口"), "Should contain 'Version接口'");

        // 验证 URL 正确（没有 controller 级前缀，直接使用 method 级路径）
        assertTrue(allContent.contains("GET /ping"), "Should contain 'GET /ping'");
        assertTrue(allContent.contains("GET /version"), "Should contain 'GET /version'");

        // 验证 Response Body 字段
        assertTrue(allContent.contains("message"), "Should contain response field 'message'");
        assertTrue(allContent.contains("响应消息"), "Should contain response field comment '响应消息'");
        assertTrue(allContent.contains("timestamp"), "Should contain response field 'timestamp'");
        assertTrue(allContent.contains("时间戳"), "Should contain response field comment '时间戳'");
    }

}

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
 * multiple-urls-per-handler 集成测试。
 *
 * <p>验证 Controller 级和 Method 级各有多个 path 时的笛卡尔组合 URL 渲染。
 *
 * @author Deolin 2026-05-15
 */
public class MultipleUrlsPerHandlerItTest extends DocAnalyzerItBaseTest {

    @Test
    void test() throws IOException {
        runDocAnalyzer("multiple-urls-per-handler");

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

        assertEquals(1, mdFiles.size(), "Should generate exactly 1 .md file");

        // 验证 handler 描述
        assertTrue(allContent.contains("查询用户"), "Should contain '查询用户'");
        assertTrue(allContent.contains("创建用户"), "Should contain '创建用户'");

        // 验证 GET handler 的笛卡尔组合 URL（2 controller paths × 2 method paths = 4 URLs）
        assertTrue(allContent.contains("/api/users/list"), "Should contain '/api/users/list'");
        assertTrue(allContent.contains("/api/users/all"), "Should contain '/api/users/all'");
        assertTrue(allContent.contains("/api/v2/users/list"), "Should contain '/api/v2/users/list'");
        assertTrue(allContent.contains("/api/v2/users/all"), "Should contain '/api/v2/users/all'");

        // 验证 POST handler 的笛卡尔组合 URL
        assertTrue(allContent.contains("/api/users/create"), "Should contain '/api/users/create'");
        assertTrue(allContent.contains("/api/users/new"), "Should contain '/api/users/new'");
        assertTrue(allContent.contains("/api/v2/users/create"), "Should contain '/api/v2/users/create'");
        assertTrue(allContent.contains("/api/v2/users/new"), "Should contain '/api/v2/users/new'");

        // 验证 URL 以「或」连接
        assertTrue(allContent.contains("或"), "Should contain '或' for alternative URLs");

        // 验证 HTTP 方法
        assertTrue(allContent.contains("GET"), "Should contain HTTP method GET");
        assertTrue(allContent.contains("POST"), "Should contain HTTP method POST");

        // 验证 Response Body 字段
        assertTrue(allContent.contains("id"), "Should contain response field 'id'");
        assertTrue(allContent.contains("username"), "Should contain response field 'username'");
    }

}

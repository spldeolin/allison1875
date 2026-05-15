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
 * no-path-controller-and-method 集成测试。
 *
 * <p>验证 controller 和 method 都没有 path 时 URL 回退为 /。
 *
 * @author Deolin 2026-05-15
 */
public class NoPathControllerAndMethodItTest extends DocAnalyzerItBaseTest {

    @Test
    void test() throws IOException {
        runDocAnalyzer("no-path-controller-and-method");

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

        // 验证 handler
        assertTrue(allContent.contains("默认首页"), "Should contain '默认首页'");

        // 验证 URL 回退为 GET /（无 controller 级 path 且无 method 级 path）
        assertTrue(allContent.contains("GET /"), "Should contain 'GET /' (fallback URL)");

        // 验证 Markdown 结构
        assertTrue(allContent.contains("### URL"), "Should contain '### URL' section");
    }

}

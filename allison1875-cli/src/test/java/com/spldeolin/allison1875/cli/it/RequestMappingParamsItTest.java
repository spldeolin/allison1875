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
 * request-mapping-params 集成测试。
 *
 * 验证 doc-analyzer 能正确处理 @RequestMapping params 条件，
 * 将 controller 级 + method 级 params 拼接到 URL 中。
 *
 * @author Deolin 2025-05-13
 */
public class RequestMappingParamsItTest extends DocAnalyzerItBaseTest {

    @Test
    void test() throws IOException {
        runDocAnalyzer("request-mapping-params");

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

        // 验证 md 文件名和数量
        assertEquals(1, mdFiles.size(), "Should generate exactly 1 .md file");
        assertEquals("配置管理.md", mdFiles.get(0).getName(), "Markdown filename should be '配置管理.md'");

        assertTrue(allContent.contains("查询配置"), "Should contain '查询配置'");

        // 验证完整的 URL 包含 params（controller 级 + method 级拼接）
        assertTrue(allContent.contains("GET /api/config?module=system&action=read"),
                "Should contain full URL 'GET /api/config?module=system&action=read'");

        // 验证 Response Body 字段
        assertTrue(allContent.contains("configKey"), "Should contain response field 'configKey'");
        assertTrue(allContent.contains("配置键"), "Should contain response field comment '配置键'");
        assertTrue(allContent.contains("configValue"), "Should contain response field 'configValue'");
        assertTrue(allContent.contains("配置值"), "Should contain response field comment '配置值'");
    }

}

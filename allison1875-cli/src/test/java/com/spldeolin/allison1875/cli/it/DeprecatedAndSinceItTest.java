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
 * deprecated-and-since 集成测试。
 *
 * 验证 doc-analyzer 能正确处理：
 * - handler 级和 controller 级的 @since 标签
 * - handler 级的 @deprecated 标签
 * - 字段级的 @since 和 @deprecated 标签
 *
 * @author Deolin 2025-05-13
 */
public class DeprecatedAndSinceItTest extends DocAnalyzerItBaseTest {

    @Test
    void test() throws IOException {
        runDocAnalyzer("deprecated-and-since");

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
        String mdContent = sb.toString();

        // 验证 md 文件名和数量
        assertEquals(1, mdFiles.size(), "Should generate exactly 1 .md file");
        assertEquals("公告管理.md", mdFiles.get(0).getName(), "Markdown filename should be '公告管理.md'");

        // 验证3个 handler 都被分析
        assertTrue(mdContent.contains("查询最新公告"), "Should contain '查询最新公告'");
        assertTrue(mdContent.contains("创建公告"), "Should contain '创建公告'");
        assertTrue(mdContent.contains("查询过期公告（已废弃）"), "Should contain '查询过期公告（已废弃）'");

        // 验证 URL
        assertTrue(mdContent.contains("GET /api/notices/latest"), "Should contain 'GET /api/notices/latest'");
        assertTrue(mdContent.contains("POST /api/notices"), "Should contain 'POST /api/notices'");
        assertTrue(mdContent.contains("GET /api/notices/expired"), "Should contain 'GET /api/notices/expired'");

        // 验证 handler 级 @since 标签 — 兼容性说明区域
        assertTrue(mdContent.contains("### 兼容性说明"), "Should contain compatibility section header");
        assertTrue(mdContent.contains("本接口加入版本：v1.0.0"), "Should contain '本接口加入版本：v1.0.0'");
        assertTrue(mdContent.contains("本接口加入版本：v2.0.0"), "Should contain '本接口加入版本：v2.0.0'");

        // 验证 handler 级 @deprecated 标签 — 兼容性说明区域
        assertTrue(mdContent.contains("本接口已过时，不建议调用，过时原因："),
                "Should contain deprecated description prefix");
        assertTrue(mdContent.contains("getLatest"), "Deprecated description should reference 'getLatest'");
        assertTrue(mdContent.contains("v4.0"), "Deprecated description should reference 'v4.0'");

        // 验证字段级 @since（priority 字段的 @since v3.0.0）
        assertTrue(mdContent.contains("本字段加入版本：v3.0.0"), "Should contain field-level @since 'v3.0.0'");

        // 验证字段级 @deprecated（category 字段的 @deprecated）
        assertTrue(mdContent.contains("本字段已过时，原因："), "Should contain field-level @deprecated prefix");
        assertTrue(mdContent.contains("tags"), "Should contain field-level @deprecated referencing 'tags'");

        // 验证 Request Body 字段
        assertTrue(mdContent.contains("title"), "Should contain field 'title'");
        assertTrue(mdContent.contains("公告标题"), "Should contain field comment '公告标题'");
        assertTrue(mdContent.contains("content"), "Should contain field 'content'");
        assertTrue(mdContent.contains("公告内容"), "Should contain field comment '公告内容'");
        assertTrue(mdContent.contains("priority"), "Should contain field 'priority'");
        assertTrue(mdContent.contains("category"), "Should contain deprecated field 'category'");
    }

}

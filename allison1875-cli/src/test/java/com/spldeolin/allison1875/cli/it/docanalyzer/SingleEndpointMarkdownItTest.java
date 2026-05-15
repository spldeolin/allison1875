package com.spldeolin.allison1875.cli.it.docanalyzer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
 * single-endpoint-markdown 集成测试。
 *
 * 验证 doc-analyzer 能正确处理：
 * - singleEndpointPerMarkdown = true 时，每个 endpoint 生成独立的 md 文件
 * - globalUrlPrefix 不以 / 开头时的处理（自动补 /）
 * - @RequestParam 的 name / value 别名
 * - @RequestParam 的 defaultValue
 * - #API-DOC-IGNORE# 字段忽略
 * - void 返回类型（无 Response Body）
 *
 * @author Deolin 2025-05-13
 */
public class SingleEndpointMarkdownItTest extends DocAnalyzerItBaseTest {

    @Test
    void test() throws IOException {
        runDocAnalyzer("single-endpoint-markdown");

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

        // singleEndpointPerMarkdown = true 时，2 个 handler 应生成 2 个 md 文件
        assertEquals(2, mdFiles.size(), "Should generate 2 .md files (one per endpoint)");

        StringBuilder sb = new StringBuilder();
        for (File mdFile : mdFiles) {
            sb.append(new String(Files.readAllBytes(mdFile.toPath()), StandardCharsets.UTF_8));
            sb.append("\n");
        }
        String allContent = sb.toString();

        // 验证两个 handler 都被分析
        assertTrue(allContent.contains("搜索物品"), "Should contain '搜索物品'");
        assertTrue(allContent.contains("提交物品"), "Should contain '提交物品'");

        // 验证 globalUrlPrefix 不以 / 开头时自动补 /（config 中 globalUrlPrefix: api → 自动补/）
        assertTrue(allContent.contains("/api/items"), "Should contain prefixed URL '/api/items'");
        assertTrue(allContent.contains("GET /api/items/search"), "Should contain 'GET /api/items/search'");
        assertTrue(allContent.contains("POST /api/items"), "Should contain 'POST /api/items'");

        // 验证 @RequestParam(name = "q") 别名
        assertTrue(allContent.contains("|q|"), "Should contain aliased query param name 'q' in table row");

        // 验证 @RequestParam(value = "page", defaultValue = "1")
        assertTrue(allContent.contains("page"), "Should contain query param 'page'");
        assertTrue(allContent.contains("1"), "Should contain defaultValue '1'");

        // 验证 #API-DOC-IGNORE# 字段不出现
        assertFalse(allContent.contains("internalTraceId"), "Should NOT contain ignored field 'internalTraceId'");

        // 验证 keyword 字段存在（请求体）
        assertTrue(allContent.contains("keyword"), "Should contain field 'keyword'");

        // 验证 void 返回类型的 handler（submitItem）不生成 Response Body 区域
        File submitMd = null;
        for (File mdFile : mdFiles) {
            String content = new String(Files.readAllBytes(mdFile.toPath()), StandardCharsets.UTF_8);
            if (content.contains("提交物品")) {
                submitMd = mdFile;
                break;
            }
        }
        assertNotNull(submitMd, "Should find md file containing '提交物品'");
        String submitContent = new String(Files.readAllBytes(submitMd.toPath()), StandardCharsets.UTF_8);
        assertFalse(submitContent.contains("Response Body"), "void handler should NOT have Response Body section");
    }

}

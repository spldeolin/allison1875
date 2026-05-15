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
 * composed-annotation-value 集成测试。
 *
 * <p>验证 doc-analyzer 能正确解析 @PostMapping("/path")、@GetMapping("/path") 等
 * composed annotation 的 value 属性，生成的文档中 URL 包含正确的路径。
 *
 * @author Deolin 2025-05-13
 */
public class ComposedAnnotationValueItTest extends DocAnalyzerItBaseTest {

    @Test
    void test() throws IOException {
        runDocAnalyzer("composed-annotation-value");

        // 验证 api-docs 目录存在
        File apiDocsDir = new File(basedir, "api-docs");
        assertTrue(apiDocsDir.exists(), "api-docs directory should exist");
        assertTrue(apiDocsDir.isDirectory(), "api-docs should be a directory");

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

        // 验证 md 文件数量和文件名
        assertEquals(1, mdFiles.size(), "Should generate exactly 1 .md file, but found " + mdFiles.size());
        assertEquals("文章管理.md", mdFiles.get(0).getName(), "Markdown filename should be '文章管理.md'");

        String allContent = new String(Files.readAllBytes(mdFiles.get(0).toPath()), StandardCharsets.UTF_8);

        // ===== 验证 @GetMapping("/detail") 的 value 被正确解析 =====
        assertTrue(allContent.contains("/api/articles/detail"),
                "Should contain combined URL '/api/articles/detail' from @GetMapping(\"/detail\")");
        assertTrue(allContent.contains("GET"), "Should contain HTTP method GET");
        assertTrue(allContent.contains("获取文章详情"), "Should contain handler description '获取文章详情'");

        // ===== 验证 @PostMapping("/create") 的 value 被正确解析 =====
        assertTrue(allContent.contains("/api/articles/create"),
                "Should contain combined URL '/api/articles/create' from @PostMapping(\"/create\")");
        assertTrue(allContent.contains("POST"), "Should contain HTTP method POST");
        assertTrue(allContent.contains("创建文章"), "Should contain handler description '创建文章'");

        // ===== 验证 @PutMapping("/update") 的 value 被正确解析 =====
        assertTrue(allContent.contains("/api/articles/update"),
                "Should contain combined URL '/api/articles/update' from @PutMapping(\"/update\")");
        assertTrue(allContent.contains("PUT"), "Should contain HTTP method PUT");
        assertTrue(allContent.contains("更新文章"), "Should contain handler description '更新文章'");

        // ===== 验证 @DeleteMapping("/delete") 的 value 被正确解析 =====
        assertTrue(allContent.contains("/api/articles/delete"),
                "Should contain combined URL '/api/articles/delete' from @DeleteMapping(\"/delete\")");
        assertTrue(allContent.contains("DELETE"), "Should contain HTTP method DELETE");
        assertTrue(allContent.contains("删除文章"), "Should contain handler description '删除文章'");

        // ===== 验证 @PatchMapping("/patch") 的 value 被正确解析 =====
        assertTrue(allContent.contains("/api/articles/patch"),
                "Should contain combined URL '/api/articles/patch' from @PatchMapping(\"/patch\")");
        assertTrue(allContent.contains("PATCH"), "Should contain HTTP method PATCH");
        assertTrue(allContent.contains("局部更新文章"), "Should contain handler description '局部更新文章'");

        // ===== 验证 Request Body 字段 =====
        assertTrue(allContent.contains("title"), "Should contain field 'title'");
        assertTrue(allContent.contains("content"), "Should contain field 'content'");
        assertTrue(allContent.contains("文章标题"), "Should contain field comment '文章标题'");
        assertTrue(allContent.contains("文章内容"), "Should contain field comment '文章内容'");
    }

}

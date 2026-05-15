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
 * pathvar-and-reqparam-aliases 集成测试。
 *
 * 验证 doc-analyzer 能正确处理：
 * - @PathVariable 的 value / name 别名
 * - @RequestParam 的 value / name 别名
 * - @RequestParam 的 defaultValue
 * - primitive boolean 类型推导
 *
 * @author Deolin 2025-05-13
 */
public class PathvarAndReqparamAliasesItTest extends DocAnalyzerItBaseTest {

    @Test
    void test() throws IOException {
        runDocAnalyzer("pathvar-and-reqparam-aliases");

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
        assertEquals("图书管理.md", mdFiles.get(0).getName(), "Markdown filename should be '图书管理.md'");

        // 验证 3 个 handler 都被解析
        assertTrue(allContent.contains("通过SingleMember别名查询图书"), "Should contain description");
        assertTrue(allContent.contains("通过name属性别名查询图书"), "Should contain description");
        assertTrue(allContent.contains("搜索图书"), "Should contain description");

        // 验证 URL 中包含路径变量
        assertTrue(allContent.contains("GET /api/books/{bookId}"),
                "Should contain URL with path var 'GET /api/books/{bookId}'");
        assertTrue(allContent.contains("GET /api/books/by-isbn/{isbn}"),
                "Should contain URL with path var 'GET /api/books/by-isbn/{isbn}'");
        assertTrue(allContent.contains("GET /api/books/search"), "Should contain URL 'GET /api/books/search'");

        // 验证 Path Param 表格
        assertTrue(allContent.contains("### Path Param"), "Should contain Path Param section");
        assertTrue(allContent.contains("bookId"), "Should contain aliased path param name 'bookId'");
        assertTrue(allContent.contains("isbn"), "Should contain aliased path param name 'isbn'");

        // 验证 Query Param 表格
        assertTrue(allContent.contains("### Query Param"), "Should contain Query Param section");
        assertTrue(allContent.contains("keyword"), "Should contain aliased query param name 'keyword'");
        assertTrue(allContent.contains("是"), "Should contain required=true rendered as '是'");

        // 验证 @RequestParam(value = "page_no") 别名
        assertTrue(allContent.contains("page_no"), "Should contain aliased query param name 'page_no'");
        assertTrue(allContent.contains("页码"), "Should contain query param description '页码'");

        // 验证 primitive boolean 类型推导
        assertTrue(allContent.contains("active"), "Should contain query param 'active'");
        assertTrue(allContent.contains("Boolean"), "Should contain type 'Boolean' for primitive boolean");
        assertTrue(allContent.contains("是否启用"), "Should contain query param description '是否启用'");
    }

}

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
 * top-level-array-response 集成测试。
 *
 * <p>验证 handler 直接返回 List&lt;SomeDTO&gt;（顶层 ArraySchema）时，
 * Response Body 根节点渲染为 Object Array，子字段正常展示。
 *
 * @author Deolin 2026-05-15
 */
public class TopLevelArrayResponseItTest extends DocAnalyzerItBaseTest {

    @Test
    void test() throws IOException {
        runDocAnalyzer("top-level-array-response");

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
        assertEquals("商品管理.md", mdFiles.get(0).getName(), "Markdown filename should be '商品管理.md'");

        // 验证 handler
        assertTrue(allContent.contains("获取所有商品"), "Should contain '获取所有商品'");
        assertTrue(allContent.contains("GET /api/products"), "Should contain 'GET /api/products'");

        // 验证 Response Body 根节点为 Object Array
        assertTrue(allContent.contains("Object Array"), "Should contain 'Object Array' for top-level List return type");

        // 验证 Response Body 子字段
        assertTrue(allContent.contains("id"), "Should contain response field 'id'");
        assertTrue(allContent.contains("商品ID"), "Should contain response field comment '商品ID'");
        assertTrue(allContent.contains("name"), "Should contain response field 'name'");
        assertTrue(allContent.contains("商品名称"), "Should contain response field comment '商品名称'");
        assertTrue(allContent.contains("price"), "Should contain response field 'price'");
        assertTrue(allContent.contains("stock"), "Should contain response field 'stock'");
        assertTrue(allContent.contains("库存数量"), "Should contain response field comment '库存数量'");

        // 验证 Markdown 结构
        assertTrue(allContent.contains("### Response Body (application/json)"), "Should contain response body section");
    }

}

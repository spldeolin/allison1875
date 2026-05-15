package com.spldeolin.allison1875.cli.it;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

/**
 * glob-regex-branches 集成测试。
 *
 * 验证 mvcHandlerQualifierWildcards 中 *, ?, {,} 等 glob 模式的正确匹配。
 *
 * @author Deolin 2025-05-13
 */
public class GlobRegexBranchesItTest extends DocAnalyzerItBaseTest {

    @Test
    void test() throws IOException {
        runDocAnalyzer("glob-regex-branches");

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

        // 验证 md 文件数量（3个 controller → 3个 md 文件）
        assertEquals(3, mdFiles.size(), "Should generate 3 .md files (one per controller)");
        List<String> fileNames = mdFiles.stream().map(File::getName).sorted().collect(Collectors.toList());
        assertTrue(fileNames.contains("商品管理.md"), "Should contain '商品管理.md'");
        assertTrue(fileNames.contains("用户管理.md"), "Should contain '用户管理.md'");
        assertTrue(fileNames.contains("订单管理.md"), "Should contain '订单管理.md'");

        // Pattern "*Controller.list*" matches all three list* handlers
        assertTrue(allContent.contains("列出订单"), "Should contain listOrders matched by *Controller.list*");
        assertTrue(allContent.contains("列出用户"), "Should contain listUsers matched by *Controller.list*");
        assertTrue(allContent.contains("列出商品"), "Should contain listProducts matched by *Controller.list*");

        // Pattern "com.example.controller.{Order,User}Controller.get?rder*" matches getOrderDetail
        assertTrue(allContent.contains("获取订单详情"),
                "Should contain getOrderDetail matched by {Order,User} and get?rder*");

        // getUserDetail should NOT be matched (get?rder* won't match getUserDetail)
        assertFalse(allContent.contains("获取用户详情"),
                "Should NOT contain getUserDetail (not matching get?rder* glob)");

        // 验证 URL
        assertTrue(allContent.contains("GET /api/orders/list"), "Should contain URL 'GET /api/orders/list'");
        assertTrue(allContent.contains("GET /api/users/list"), "Should contain URL 'GET /api/users/list'");
        assertTrue(allContent.contains("GET /api/products/list"), "Should contain URL 'GET /api/products/list'");
        assertTrue(allContent.contains("GET /api/orders/detail"), "Should contain URL 'GET /api/orders/detail'");
    }

}

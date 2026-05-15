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
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

/**
 * horizontal-domains 集成测试。
 * 水平划分：配置 2 个 domain（user & order），使用 domain=user 选择 user 领域运行。
 * 在单模块水平划分场景下，doc-analyzer 会遍历同一 sourceRoot 下的所有 controller，
 * 不按 controllerPackage 过滤，因此 order domain 的 controller 也会被检测到。
 *
 * @author Deolin 2026-05-13
 */
public class HorizontalDomainsItTest extends DocAnalyzerItBaseTest {

    @Test
    void test() throws IOException {
        runDocAnalyzer("horizontal-domains", "user");

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

        // 验证 md 文件数量（两个 controller → 两个 md 文件）
        assertEquals(2, mdFiles.size(), "Should generate 2 .md files");
        List<String> fileNames = mdFiles.stream().map(File::getName).sorted().collect(Collectors.toList());
        assertTrue(fileNames.contains("用户管理.md"), "Should contain '用户管理.md'");
        assertTrue(fileNames.contains("订单管理.md"), "Should contain '订单管理.md'");

        // user domain's endpoints should be present
        assertTrue(allContent.contains("创建用户"), "Should contain user domain handler '创建用户'");
        assertTrue(allContent.contains("POST /api/users"), "Should contain user domain URL 'POST /api/users'");
        assertTrue(allContent.contains("username"), "Should contain user request field 'username'");
        assertTrue(allContent.contains("用户名"), "Should contain user request field comment '用户名'");

        // In single-module horizontal split, ALL controllers in sourceRoot are detected (expected behavior)
        assertTrue(allContent.contains("创建订单"),
                "OrderController should also be detected in single-module scenario");
        assertTrue(allContent.contains("POST /api/orders"), "Should contain order domain URL 'POST /api/orders'");
        assertTrue(allContent.contains("productId"), "Should contain order request field 'productId'");
        assertTrue(allContent.contains("商品ID"), "Should contain order request field comment '商品ID'");
    }

}
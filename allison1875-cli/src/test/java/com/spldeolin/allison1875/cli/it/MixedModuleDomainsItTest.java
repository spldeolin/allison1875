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
 * mixed-module-domains 集成测试。
 * 混合划分：多 domain + 垂直拆分。
 * user domain: controller 在当前模块，DTO 在 user-api/
 * order domain: controller 在当前模块，DTO 在 order-api/
 * 使用 domain=order 运行 order 领域。
 *
 * @author Deolin 2026-05-13
 */
public class MixedModuleDomainsItTest extends DocAnalyzerItBaseTest {

    @Test
    void test() throws IOException {
        runDocAnalyzer("mixed-module-domains", "order");

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

        // 验证 md 文件数量（both controllers detected in same sourceRoot）
        assertEquals(2, mdFiles.size(), "Should generate 2 .md files (both controllers detected in same sourceRoot)");

        // Order domain's endpoints should be present
        assertTrue(allContent.contains("下单"), "Should contain order domain handler '下单'");
        assertTrue(allContent.contains("POST /api/orders"), "Should contain 'POST /api/orders'");

        // Order DTO fields from order-api module should be resolved
        assertTrue(allContent.contains("productId"), "Should contain order request field 'productId'");
        assertTrue(allContent.contains("shippingAddress"), "Should contain order request field 'shippingAddress'");
        assertTrue(allContent.contains("quantity"), "Should contain order request field 'quantity'");

        // Response fields from order-api
        assertTrue(allContent.contains("orderNo"), "Should contain order response field 'orderNo'");
    }

}
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
import org.junit.jupiter.api.Test;

/**
 * primitive-and-simple-return 集成测试。
 *
 * 验证 doc-analyzer 能正确处理 primitive/简单类型返回值（String, Integer, Boolean），
 * 以及 @return Javadoc 描述提取。
 *
 * @author Deolin 2025-05-13
 */
public class PrimitiveAndSimpleReturnItTest extends DocAnalyzerItBaseTest {

    @Test
    void test() throws IOException {
        runDocAnalyzer("primitive-and-simple-return");

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
        assertEquals("健康检查.md", mdFiles.get(0).getName(), "Markdown filename should be '健康检查.md'");

        // 验证所有 handler
        assertTrue(allContent.contains("返回纯字符串"), "Should contain '返回纯字符串'");
        assertTrue(allContent.contains("返回计数"), "Should contain '返回计数'");
        assertTrue(allContent.contains("返回是否健康"), "Should contain '返回是否健康'");

        // 验证 URL
        assertTrue(allContent.contains("GET /api/health/status"), "Should contain 'GET /api/health/status'");
        assertTrue(allContent.contains("GET /api/health/count"), "Should contain 'GET /api/health/count'");
        assertTrue(allContent.contains("GET /api/health/alive"), "Should contain 'GET /api/health/alive'");

        // 验证 String 返回类型被正确解析（isValueTypeSchema 分支）
        assertTrue(allContent.contains("| | String |"), "Should contain simple String value type schema row");

        // 验证 Integer 返回类型
        assertTrue(allContent.contains("| | Integer |"), "Should contain simple Integer value type schema row");

        // 验证 Boolean 返回类型
        assertTrue(allContent.contains("| | Boolean |"), "Should contain simple Boolean value type schema row");

        // 验证 @return 描述被提取
        assertTrue(allContent.contains("健康状态"), "Should contain @return description '健康状态'");
        assertTrue(allContent.contains("在线用户数"), "Should contain @return description '在线用户数'");
        assertTrue(allContent.contains("是否正常"), "Should contain @return description '是否正常'");

        // 验证没有 Request Body 部分（三个都是 GET 且没有 @RequestBody）
        assertFalse(allContent.contains("Request Body"),
                "Should NOT contain 'Request Body' for GET handlers without request body");
    }

}

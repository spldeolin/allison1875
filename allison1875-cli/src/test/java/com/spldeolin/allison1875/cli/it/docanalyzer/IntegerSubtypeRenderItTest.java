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
 * integer-subtype-render 集成测试。
 *
 * <p>验证 Long、Short、Byte 等不同整数类型在 Markdown 中渲染为对应的 JSON 类型名称。
 *
 * @author Deolin 2026-05-15
 */
public class IntegerSubtypeRenderItTest extends DocAnalyzerItBaseTest {

    @Test
    void test() throws IOException {
        runDocAnalyzer("integer-subtype-render");

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

        assertEquals(1, mdFiles.size(), "Should generate exactly 1 .md file");

        // 验证 handler
        assertTrue(allContent.contains("获取整数类型示例"), "Should contain handler description");
        assertTrue(allContent.contains("GET /api/integers"), "Should contain 'GET /api/integers'");

        // 验证各个整数类型字段存在
        assertTrue(allContent.contains("longValue"), "Should contain field 'longValue'");
        assertTrue(allContent.contains("shortValue"), "Should contain field 'shortValue'");
        assertTrue(allContent.contains("byteValue"), "Should contain field 'byteValue'");
        assertTrue(allContent.contains("primitiveLong"), "Should contain field 'primitiveLong'");
        assertTrue(allContent.contains("primitiveShort"), "Should contain field 'primitiveShort'");
        assertTrue(allContent.contains("primitiveByte"), "Should contain field 'primitiveByte'");

        // 验证 Markdown 结构
        assertTrue(allContent.contains("### Response Body (application/json)"), "Should contain response body section");
    }

}

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
 * not-empty-and-negative-validation 集成测试。
 *
 * <p>验证 @NotEmpty、@Negative、@FutureOrPresent、@PastOrPresent 校验注解的文档化。
 *
 * @author Deolin 2026-05-15
 */
public class NotEmptyAndNegativeValidationItTest extends DocAnalyzerItBaseTest {

    @Test
    void test() throws IOException {
        runDocAnalyzer("not-empty-and-negative-validation");

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
        assertTrue(allContent.contains("提交校验"), "Should contain '提交校验'");
        assertTrue(allContent.contains("POST /api/advanced-valid"), "Should contain 'POST /api/advanced-valid'");

        // 验证字段
        assertTrue(allContent.contains("tags"), "Should contain field 'tags'");
        assertTrue(allContent.contains("标签列表"), "Should contain field comment '标签列表'");
        assertTrue(allContent.contains("negativeValue"), "Should contain field 'negativeValue'");
        assertTrue(allContent.contains("负数值"), "Should contain field comment '负数值'");
        assertTrue(allContent.contains("startDate"), "Should contain field 'startDate'");
        assertTrue(allContent.contains("开始日期"), "Should contain field comment '开始日期'");
        assertTrue(allContent.contains("endDate"), "Should contain field 'endDate'");
        assertTrue(allContent.contains("结束日期"), "Should contain field comment '结束日期'");

        // 验证 @NotEmpty
        assertTrue(allContent.contains("必须有元素/字符"), "Should contain @NotEmpty description");

        // 验证 @Negative
        assertTrue(allContent.contains("必须是负数"), "Should contain @Negative description");

        // 验证 @FutureOrPresent
        assertTrue(allContent.contains("必须是未来或现在"), "Should contain @FutureOrPresent description");

        // 验证 @PastOrPresent
        assertTrue(allContent.contains("必须是过去或现在"), "Should contain @PastOrPresent description");

        // 验证 Markdown 结构
        assertTrue(allContent.contains("### Request Body (application/json)"), "Should contain request body section");
        assertTrue(allContent.contains("### Response Body (application/json)"), "Should contain response body section");
    }

}

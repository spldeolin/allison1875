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
 * enum-array-field 集成测试。
 *
 * <p>验证 SomeEnum[] 类型字段的枚举项解析（isArray() 分支）。
 *
 * @author Deolin 2026-05-15
 */
public class EnumArrayFieldItTest extends DocAnalyzerItBaseTest {

    @Test
    void test() throws IOException {
        runDocAnalyzer("enum-array-field");

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
        assertTrue(allContent.contains("提交权限"), "Should contain '提交权限'");
        assertTrue(allContent.contains("POST /api/array-enum"), "Should contain 'POST /api/array-enum'");

        // 验证字段
        assertTrue(allContent.contains("permissions"), "Should contain field 'permissions'");
        assertTrue(allContent.contains("权限列表"), "Should contain field comment '权限列表'");

        // 验证枚举项出现在文档中（PermissionEnum 的 code : title 格式）
        assertTrue(allContent.contains("1 : 读取"), "Should contain enum constant '1 : 读取'");
        assertTrue(allContent.contains("2 : 写入"), "Should contain enum constant '2 : 写入'");
        assertTrue(allContent.contains("3 : 管理"), "Should contain enum constant '3 : 管理'");

        // 验证 Response 中也包含枚举项
        String respSection = allContent.substring(allContent.indexOf("Response Body"));
        assertTrue(respSection.contains("1 : 读取"), "Response Body should also contain enum constants");

        // 验证 Markdown 结构
        assertTrue(allContent.contains("### Request Body (application/json)"), "Should contain request body section");
        assertTrue(allContent.contains("### Response Body (application/json)"), "Should contain response body section");
    }

}

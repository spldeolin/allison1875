package com.spldeolin.allison1875.cli.it.docanalyzer;

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
 * enum-without-code-title 集成测试。
 *
 * <p>验证枚举类不具备 getCode()/getTitle() 方法时，枚举项不出现在文档中（而非报错）。
 *
 * @author Deolin 2026-05-15
 */
public class EnumWithoutCodeTitleItTest extends DocAnalyzerItBaseTest {

    @Test
    void test() throws IOException {
        runDocAnalyzer("enum-without-code-title");

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
        assertTrue(allContent.contains("更新状态"), "Should contain '更新状态'");
        assertTrue(allContent.contains("POST /api/status"), "Should contain 'POST /api/status'");

        // 验证字段存在
        assertTrue(allContent.contains("status"), "Should contain field 'status'");
        assertTrue(allContent.contains("remark"), "Should contain field 'remark'");

        // 验证无枚举项渲染（ACTIVE, INACTIVE, PENDING 不应出现为 code: title 格式）
        assertFalse(allContent.contains("ACTIVE"), "Should NOT contain bare enum name 'ACTIVE'");
        assertFalse(allContent.contains("INACTIVE"), "Should NOT contain bare enum name 'INACTIVE'");

        // 验证 Markdown 结构
        assertTrue(allContent.contains("### Request Body (application/json)"), "Should contain request body section");
    }

}

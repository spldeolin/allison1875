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
 * dependency-dirs 集成测试。
 *
 * <p>验证 dependencyDirsOrJavaFilePath 配置能正确加载外部目录中的Java源码（external-dto/）。
 *
 * @author Deolin 2026-05-13
 */
public class DependencyDirsItTest extends DocAnalyzerItBaseTest {

    @Test
    void test() throws IOException {
        runDocAnalyzer("dependency-dirs");

        // 验证 api-docs 目录存在
        File apiDocsDir = new File(basedir, "api-docs");
        assertTrue(apiDocsDir.exists(), "api-docs directory should exist");

        // 查找 md 文件
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

        // 验证 md 文件名和数量
        assertEquals(1, mdFiles.size(), "Should generate exactly 1 .md file");
        assertEquals("审计管理.md", mdFiles.get(0).getName(), "Markdown filename should be '审计管理.md'");

        // 合并所有 md 内容
        StringBuilder sb = new StringBuilder();
        for (File mdFile : mdFiles) {
            sb.append(new String(Files.readAllBytes(mdFile.toPath()), StandardCharsets.UTF_8));
            sb.append("\n");
        }
        String allContent = sb.toString();

        // 验证内容包含预期文本
        assertTrue(allContent.contains("查询审计信息"), "Should contain '查询审计信息'");
        assertTrue(allContent.contains("GET /api/audit"), "Should contain 'GET /api/audit'");

        // 验证 Response Body 字段
        assertTrue(allContent.contains("auditId"), "Should contain field 'auditId'");
        assertTrue(allContent.contains("审计ID"), "Should contain field comment '审计ID'");
        assertTrue(allContent.contains("actionType"), "Should contain field 'actionType'");
        assertTrue(allContent.contains("操作类型"), "Should contain field comment '操作类型'");

        // 验证没有 Request Body（GET 无 @RequestBody）
        assertFalse(allContent.contains("Request Body"),
                "Should NOT contain 'Request Body' for GET handler without request body");
    }

}
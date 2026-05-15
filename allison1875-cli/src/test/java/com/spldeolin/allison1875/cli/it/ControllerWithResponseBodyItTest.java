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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;

/**
 * controller-with-response-body 集成测试。
 *
 * <p>验证 @Controller + @ResponseBody（非 @RestController）的场景，
 * 确保带有 @ResponseBody 注解的 handler 方法能被正确解析并生成 API 文档。
 *
 * @author Deolin 2026-05-13
 */
public class ControllerWithResponseBodyItTest extends DocAnalyzerItBaseTest {

    @Test
    void test() throws IOException {
        runDocAnalyzer("controller-with-response-body");

        File apiDocsDir = new File(basedir, "api-docs");
        assertTrue(apiDocsDir.exists(), "api-docs directory should exist");

        // 查找所有 md 文件
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

        // 将所有 md 内容合并，方便统一断言
        StringBuilder sb = new StringBuilder();
        for (File mdFile : mdFiles) {
            sb.append(new String(Files.readAllBytes(mdFile.toPath()), StandardCharsets.UTF_8));
            sb.append("\n");
        }
        String allContent = sb.toString();

        // 验证 md 文件名和数量
        assertEquals(1, mdFiles.size(), "Should generate exactly 1 .md file, but found " + mdFiles.size());
        assertEquals("报表管理.md", mdFiles.get(0).getName(), "Markdown filename should be '报表管理.md'");

        // 验证两个有 @ResponseBody 的 handler 都被解析
        assertTrue(allContent.contains("查询报表（有@ResponseBody）"),
                "Markdown should contain '查询报表（有@ResponseBody）'");
        assertTrue(allContent.contains("创建报表（有@ResponseBody）"),
                "Markdown should contain '创建报表（有@ResponseBody）'");

        // 验证 HTTP 方法
        assertTrue(allContent.contains("GET /api/reports"), "Markdown should contain 'GET /api/reports'");
        assertTrue(allContent.contains("POST /api/reports"), "Markdown should contain 'POST /api/reports'");

        // 验证 Response Body 字段
        assertTrue(allContent.contains("reportName"), "Markdown should contain response field 'reportName'");
        assertTrue(allContent.contains("报表名称"), "Markdown should contain response field comment '报表名称'");
        assertTrue(allContent.contains("id"), "Markdown should contain response field 'id'");

        // 验证 Request Body 字段
        assertTrue(allContent.contains("reportType"), "Markdown should contain request field 'reportType'");
        assertTrue(allContent.contains("报表类型"), "Markdown should contain request field comment '报表类型'");

        // 验证 Markdown 结构（两个 handler 都有 Response Body）
        Pattern respBodyPattern = Pattern.compile("Response Body");
        Matcher matcher = respBodyPattern.matcher(allContent);
        int respBodyCount = 0;
        while (matcher.find()) {
            respBodyCount++;
        }
        assertEquals(2, respBodyCount,
                "Should have 2 'Response Body' sections (both handlers have @ResponseBody), but found "
                        + respBodyCount);
    }

}

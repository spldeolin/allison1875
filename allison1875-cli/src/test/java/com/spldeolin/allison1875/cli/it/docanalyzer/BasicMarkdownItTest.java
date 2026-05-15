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
 * basic-markdown 集成测试。
 *
 * <p>验证 doc-analyzer 以 MARKDOWN 模式输出后，api-docs/ 目录下生成了正确的 .md 文件。
 *
 * @author Deolin 2026-05-13
 */
public class BasicMarkdownItTest extends DocAnalyzerItBaseTest {

    @Test
    void test() throws IOException {
        runDocAnalyzer("basic-markdown");

        File apiDocsDir = new File(basedir, "api-docs");
        assertTrue(apiDocsDir.exists(), "api-docs directory should exist");
        assertTrue(apiDocsDir.isDirectory(), "api-docs should be a directory");

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

        // 验证精确生成了1个 md 文件，文件名来自 controller javadoc 首行
        assertEquals(1, mdFiles.size(), "Should generate exactly 1 .md file, but found " + mdFiles.size());
        assertEquals("用户管理.md", mdFiles.get(0).getName(), "Markdown filename should be '用户管理.md'");

        // 验证包含 GET handler 的文档
        assertTrue(allContent.contains("GET"), "Markdown should contain HTTP method GET");
        assertTrue(allContent.contains("/api/users"), "Markdown should contain URL /api/users");
        assertTrue(allContent.contains("查询用户列表"), "Markdown should contain handler description '查询用户列表'");
        assertTrue(allContent.contains("根据关键字搜索用户"), "Markdown should contain handler sub-description");

        // 验证包含 POST handler 的文档
        assertTrue(allContent.contains("POST"), "Markdown should contain HTTP method POST");
        assertTrue(allContent.contains("创建用户"), "Markdown should contain handler description '创建用户'");

        // 验证 Request Body 字段出现在文档中
        assertTrue(allContent.contains("username"), "Markdown should contain field 'username'");
        assertTrue(allContent.contains("age"), "Markdown should contain field 'age'");
        assertTrue(allContent.contains("email"), "Markdown should contain field 'email'");
        assertTrue(allContent.contains("用户名"), "Markdown should contain field comment '用户名'");

        // 验证校验注解的文档化
        assertTrue(allContent.contains("必须有非空格字符"), "Markdown should contain @NotBlank validation description");
        assertTrue(allContent.contains("不能为null"), "Markdown should contain @NotNull validation description");

        // 验证 Response Body 字段（List<UserResp> → Object Array）
        assertTrue(allContent.contains("id"), "Markdown should contain response field 'id'");
        assertTrue(allContent.contains("Object Array"), "Markdown should contain 'Object Array' for List return type");
        assertTrue(allContent.contains("用户ID"), "Markdown should contain response field comment '用户ID'");

        // 验证 Query Param 出现（keyword 参数）
        assertTrue(allContent.contains("keyword"), "Markdown should contain query param 'keyword'");
        assertTrue(allContent.contains("否"), "Markdown should contain 'required=false' rendered as '否'");

        // 验证 Markdown 结构标记
        assertTrue(allContent.contains("### URL"), "Markdown should contain '### URL' section header");
        assertTrue(allContent.contains("### Query Param"), "Markdown should contain '### Query Param' section header");
        assertTrue(allContent.contains("### Request Body (application/json)"),
                "Markdown should contain request body section");
        assertTrue(allContent.contains("### Response Body (application/json)"),
                "Markdown should contain response body section");
        assertTrue(allContent.contains("---"), "Markdown should contain separator '---'");
    }

}

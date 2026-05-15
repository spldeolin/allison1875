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
 * jakarta-validation 集成测试。
 *
 * <p>验证 doc-analyzer 对 Jakarta Validation 注解（jakarta.validation.constraints.*）的文档化输出，
 * 包括 @NotBlank、@Size、@NotNull、@Min、@Max、@Past 等注解的约束描述。
 *
 * @author Deolin 2026-05-13
 */
public class JakartaValidationItTest extends DocAnalyzerItBaseTest {

    @Test
    void test() throws IOException {
        runDocAnalyzer("jakarta-validation");

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

        // 合并所有 md 内容
        StringBuilder sb = new StringBuilder();
        for (File mdFile : mdFiles) {
            sb.append(new String(Files.readAllBytes(mdFile.toPath()), StandardCharsets.UTF_8));
            sb.append("\n");
        }
        String allContent = sb.toString();

        // 验证 md 文件名和数量
        assertEquals(1, mdFiles.size(), "Should generate exactly 1 .md file, but found " + mdFiles.size());
        assertEquals("会员管理.md", mdFiles.get(0).getName(), "Markdown filename should be '会员管理.md'");

        // Endpoint detected
        assertTrue(allContent.contains("创建会员"), "Should contain '创建会员'");
        assertTrue(allContent.contains("POST /api/members"), "Should contain 'POST /api/members'");

        // jakarta.validation.constraints.NotBlank + @Size(min=2, max=50)
        assertTrue(allContent.contains("memberName"), "Should contain 'memberName'");
        assertTrue(allContent.contains("会员名称"), "Should contain field comment '会员名称'");
        assertTrue(allContent.contains("必须有非空格字符"), "Should contain @NotBlank description");
        assertTrue(allContent.contains("最小长度/容量：2"), "Should contain @Size min=2");
        assertTrue(allContent.contains("最大长度/容量：50"), "Should contain @Size max=50");

        // jakarta.validation.constraints.NotNull + @Min(1) + @Max(5)
        assertTrue(allContent.contains("不能为null"), "Should contain @NotNull description");
        assertTrue(allContent.contains("最小值：1"), "Should contain @Min(1)");
        assertTrue(allContent.contains("最大值：5"), "Should contain @Max(5)");

        // jakarta.validation.constraints.Past
        assertTrue(allContent.contains("必须是过去"), "Should contain @Past validator description");
        assertTrue(allContent.contains("birthday"), "Should contain field 'birthday'");
        assertTrue(allContent.contains("出生日期"), "Should contain field comment '出生日期'");

        // 验证 Response Body
        assertTrue(allContent.contains("id"), "Should contain response field 'id'");
        assertTrue(allContent.contains("level"), "Should contain response field 'level'");
        assertTrue(allContent.contains("会员等级"), "Should contain field comment '会员等级'");
    }

}
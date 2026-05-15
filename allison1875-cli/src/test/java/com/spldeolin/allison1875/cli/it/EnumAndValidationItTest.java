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
 * enum-and-validation 集成测试。
 *
 * 验证 doc-analyzer 能正确处理：
 * - 枚举字段的枚举项分析（getCode / getTitle 方法）
 * - @Size, @Min, @Max, @Pattern, @NotBlank, @NotNull 等校验注解
 * - Response Body 中也包含枚举项
 *
 * @author Deolin 2025-05-13
 */
public class EnumAndValidationItTest extends DocAnalyzerItBaseTest {

    @Test
    void test() throws IOException {
        runDocAnalyzer("enum-and-validation");

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
        String mdContent = sb.toString();

        // 验证 md 文件名和数量
        assertEquals(1, mdFiles.size(), "Should generate exactly 1 .md file");
        assertEquals("任务管理.md", mdFiles.get(0).getName(), "Markdown filename should be '任务管理.md'");

        // 验证 handler
        assertTrue(mdContent.contains("创建任务"), "Should contain '创建任务'");
        assertTrue(mdContent.contains("POST /api/tasks"), "Should contain 'POST /api/tasks'");

        // 验证枚举项出现在文档中（TaskStatusEnum 的 code : title 格式）
        assertTrue(mdContent.contains("1 : 待处理"), "Should contain enum constant '1 : 待处理'");
        assertTrue(mdContent.contains("2 : 处理中"), "Should contain enum constant '2 : 处理中'");
        assertTrue(mdContent.contains("3 : 已完成"), "Should contain enum constant '3 : 已完成'");
        assertTrue(mdContent.contains("4 : 已取消"), "Should contain enum constant '4 : 已取消'");

        // 验证 Response Body 中也出现枚举项
        String respSection = mdContent.substring(mdContent.indexOf("Response Body"));
        assertTrue(respSection.contains("1 : 待处理"), "Response Body should also contain enum constants");

        // 验证 @NotBlank + @Size(min=1, max=200) 校验
        assertTrue(mdContent.contains("必须有非空格字符"), "Should contain @NotBlank description");
        assertTrue(mdContent.contains("最小长度/容量：1"), "Should contain @Size min=1");
        assertTrue(mdContent.contains("最大长度/容量：200"), "Should contain @Size max=200");

        // 验证 @Size(max=2000)
        assertTrue(mdContent.contains("最大长度/容量：2000"), "Should contain @Size max=2000");

        // 验证 @NotNull + @Min + @Max 校验
        assertTrue(mdContent.contains("不能为null"), "Should contain @NotNull");
        assertTrue(mdContent.contains("最小值：1"), "Should contain @Min(1)");
        assertTrue(mdContent.contains("最大值：10"), "Should contain @Max(10)");

        // 验证 @Pattern 校验（正则表达式）
        assertTrue(mdContent.contains("正则表达式"), "Should contain @Pattern description prefix");
        assertTrue(mdContent.contains("TASK-"), "Should contain @Pattern regexp reference 'TASK-'");

        // 验证基本字段及其注释
        assertTrue(mdContent.contains("title"), "Should contain field 'title'");
        assertTrue(mdContent.contains("任务标题"), "Should contain field comment '任务标题'");
        assertTrue(mdContent.contains("description"), "Should contain field 'description'");
        assertTrue(mdContent.contains("任务描述"), "Should contain field comment '任务描述'");
        assertTrue(mdContent.contains("status"), "Should contain field 'status'");
        assertTrue(mdContent.contains("任务状态"), "Should contain field comment '任务状态'");
        assertTrue(mdContent.contains("priority"), "Should contain field 'priority'");
        assertTrue(mdContent.contains("budget"), "Should contain field 'budget'");
        assertTrue(mdContent.contains("预算金额"), "Should contain field comment '预算金额'");
        assertTrue(mdContent.contains("taskCode"), "Should contain field 'taskCode'");
        assertTrue(mdContent.contains("任务编号"), "Should contain field comment '任务编号'");
    }

}

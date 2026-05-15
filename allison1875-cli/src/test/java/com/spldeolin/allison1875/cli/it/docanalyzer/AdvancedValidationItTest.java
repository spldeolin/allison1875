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
 * advanced-validation 集成测试。
 *
 * <p>验证 doc-analyzer 对各种高级校验注解（@NotBlank、@Length、@DecimalMin、@DecimalMax、@Digits、
 *
 * @author Deolin 2026-05-13
 * @Future、@Positive、集合元素校验等）的文档化输出。
 */
public class AdvancedValidationItTest extends DocAnalyzerItBaseTest {

    @Test
    void test() throws IOException {
        runDocAnalyzer("advanced-validation");

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

        // 验证 handler 描述
        assertTrue(allContent.contains("创建支付"), "Should contain '创建支付'");

        // 验证 md 文件名和数量
        assertEquals(1, mdFiles.size(), "Should generate exactly 1 .md file, but found " + mdFiles.size());
        assertEquals("支付管理.md", mdFiles.get(0).getName(), "Markdown filename should be '支付管理.md'");

        // 验证 URL 和 HTTP 方法
        assertTrue(allContent.contains("POST /api/payments"), "Should contain 'POST /api/payments'");

        // @NotBlank + @Length(min=1, max=100)
        assertTrue(allContent.contains("必须有非空格字符"), "Should contain @NotBlank description");
        assertTrue(allContent.contains("最小长度/容量：1"), "Should contain @Length min=1");
        assertTrue(allContent.contains("最大长度/容量：100"), "Should contain @Length max=100");

        // @NotNull + @DecimalMin("0.01") + @DecimalMax("999999.99") + @Digits(integer=6, fraction=2)
        assertTrue(allContent.contains("不能为null"), "Should contain @NotNull description");
        assertTrue(allContent.contains("最小值：0.01"), "Should contain @DecimalMin value");
        assertTrue(allContent.contains("最大值：999999.99"), "Should contain @DecimalMax value");
        assertTrue(allContent.contains("最大整数位数：6"), "Should contain @Digits integer=6");
        assertTrue(allContent.contains("最大小数位数：2"), "Should contain @Digits fraction=2");

        // @Future
        assertTrue(allContent.contains("必须是未来"), "Should contain @Future validator description");

        // @Positive
        assertTrue(allContent.contains("必须是正数"), "Should contain @Positive validator description");

        // 集合元素上的校验 List<@NotBlank @Length(max=20) String>
        assertTrue(allContent.contains("tags"), "Should contain field 'tags'");
        assertTrue(allContent.contains("String Array"), "Should contain type 'String Array' for List<String>");
        assertTrue(allContent.contains("列表内元素必须有非空格字符"), "Should contain collection element @NotBlank");
        assertTrue(allContent.contains("列表内元素最大长度/容量：20"),
                "Should contain collection element @Length max=20");

        // 验证 Request Body 字段注释
        assertTrue(allContent.contains("支付描述"), "Should contain field comment '支付描述'");
        assertTrue(allContent.contains("支付金额"), "Should contain field comment '支付金额'");
        assertTrue(allContent.contains("预计支付时间"), "Should contain field comment '预计支付时间'");
        assertTrue(allContent.contains("支付笔数"), "Should contain field comment '支付笔数'");
        assertTrue(allContent.contains("标签列表"), "Should contain field comment '标签列表'");

        // 验证 Response Body 字段
        assertTrue(allContent.contains("id"), "Should contain response field 'id'");
        assertTrue(allContent.contains("amount"), "Should contain response field 'amount'");
        assertTrue(allContent.contains("status"), "Should contain response field 'status'");
        assertTrue(allContent.contains("状态"), "Should contain response field comment '状态'");
    }

}
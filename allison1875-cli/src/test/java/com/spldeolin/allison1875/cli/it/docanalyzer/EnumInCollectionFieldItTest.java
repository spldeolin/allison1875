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
 * enum-in-collection-field 集成测试。
 *
 * <p>验证 List&lt;SomeEnum&gt; 类型字段的枚举项解析（Collection 内部泛型递归分支）。
 *
 * @author Deolin 2026-05-15
 */
public class EnumInCollectionFieldItTest extends DocAnalyzerItBaseTest {

    @Test
    void test() throws IOException {
        runDocAnalyzer("enum-in-collection-field");

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
        assertTrue(allContent.contains("批量更新状态"), "Should contain '批量更新状态'");
        assertTrue(allContent.contains("POST /api/batch"), "Should contain 'POST /api/batch'");

        // 验证字段
        assertTrue(allContent.contains("operations"), "Should contain field 'operations'");
        assertTrue(allContent.contains("操作类型列表"), "Should contain field comment '操作类型列表'");
        assertTrue(allContent.contains("targetIds"), "Should contain field 'targetIds'");

        // 验证 List<OperationTypeEnum> 中枚举项出现在文档中
        // FIXME: Collection 泛型递归分支当前未提取枚举项，待确认是否为 BUG
        assertTrue(allContent.contains("1 : 新增"), "Should contain enum constant '1 : 新增'");
        assertTrue(allContent.contains("2 : 修改"), "Should contain enum constant '2 : 修改'");
        assertTrue(allContent.contains("3 : 删除"), "Should contain enum constant '3 : 删除'");

        // 验证 Response 中也包含枚举项
        String respSection = allContent.substring(allContent.indexOf("Response Body"));
        assertTrue(respSection.contains("1 : 新增"), "Response Body should also contain enum constants");

        // 验证 Response 字段
        assertTrue(allContent.contains("successCount"), "Should contain response field 'successCount'");
        assertTrue(allContent.contains("成功数量"), "Should contain response field comment '成功数量'");

        // 验证 Markdown 结构
        assertTrue(allContent.contains("### Request Body (application/json)"), "Should contain request body section");
        assertTrue(allContent.contains("### Response Body (application/json)"), "Should contain response body section");
    }

}

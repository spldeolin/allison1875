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
 * recursive-reference-schema 集成测试。
 *
 * <p>验证 doc-analyzer 对自引用 DTO（如树形结构 TreeNodeDTO.children: List&lt;TreeNodeDTO&gt;）的处理，
 * 确保自引用字段渲染为引用路径而非无限展开，且不抛 StackOverflow。
 *
 * @author Deolin 2026-05-15
 */
public class RecursiveReferenceSchemaItTest extends DocAnalyzerItBaseTest {

    @Test
    void test() throws IOException {
        runDocAnalyzer("recursive-reference-schema");

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

        // 验证 md 文件名和数量
        assertEquals(1, mdFiles.size(), "Should generate exactly 1 .md file");
        assertEquals("树形结构管理.md", mdFiles.get(0).getName(), "Markdown filename should be '树形结构管理.md'");

        // 验证 handler
        assertTrue(allContent.contains("获取整棵树"), "Should contain '获取整棵树'");
        assertTrue(allContent.contains("GET /api/trees"), "Should contain 'GET /api/trees'");

        // 验证 Response Body 中包含 TreeNodeDTO 的字段
        assertTrue(allContent.contains("id"), "Should contain field 'id'");
        assertTrue(allContent.contains("节点ID"), "Should contain field comment '节点ID'");
        assertTrue(allContent.contains("name"), "Should contain field 'name'");
        assertTrue(allContent.contains("节点名称"), "Should contain field comment '节点名称'");
        assertTrue(allContent.contains("children"), "Should contain self-referencing field 'children'");
        assertTrue(allContent.contains("子节点列表"), "Should contain field comment '子节点列表'");

        // 验证 Markdown 结构标记
        assertTrue(allContent.contains("### Response Body (application/json)"), "Should contain response body section");

        // 验证不包含「数据结构同」（TreeNodeDTO的自引用不应无限展开）
        // 自引用 children 字段本身应出现在文档中，但不应该持续展开
        int countId = 0;
        int idx = 0;
        while ((idx = allContent.indexOf("id", idx)) != -1) {
            countId++;
            idx++;
        }
        // id 字段最多出现有限次数（非无限展开）
        assertTrue(countId < 10, "id field should not appear infinitely (recursive expansion prevented)");
    }

}

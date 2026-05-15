package com.spldeolin.allison1875.cli.it;

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
 * mvc-handler-wildcards 集成测试。
 *
 * 验证 mvcHandlerQualifierWildcards 过滤：
 * - 只有匹配 *.list* 或 *.get{ById,Detail} 的 handler 出现在文档中
 * - createAnimal 和 deleteAnimal 不应出现在文档中
 *
 * @author Deolin 2025-05-13
 */
public class MvcHandlerWildcardsItTest extends DocAnalyzerItBaseTest {

    @Test
    void test() throws IOException {
        runDocAnalyzer("mvc-handler-wildcards");

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
        assertEquals("动物管理.md", mdFiles.get(0).getName(), "Markdown filename should be '动物管理.md'");

        // 验证匹配的 handler 出现在文档中
        assertTrue(allContent.contains("查询动物列表"), "Should contain '查询动物列表' (listAnimals matches *.list*)");
        assertTrue(allContent.contains("根据ID查询动物详情"),
                "Should contain '根据ID查询动物详情' (getById matches *.get{ById,Detail})");

        // 验证匹配 handler 的 URL
        assertTrue(allContent.contains("GET /api/animals"), "Should contain 'GET /api/animals'");
        assertTrue(allContent.contains("GET /api/animals/{id}"), "Should contain 'GET /api/animals/{id}'");

        // 验证匹配 handler 的 Path Param
        assertTrue(allContent.contains("### Path Param"), "Should contain Path Param section for getById");

        // 验证匹配 handler 的 Response Body 字段
        assertTrue(allContent.contains("name"), "Should contain response field 'name'");
        assertTrue(allContent.contains("动物名称"), "Should contain response field comment '动物名称'");
        assertTrue(allContent.contains("species"), "Should contain response field 'species'");
        assertTrue(allContent.contains("动物种类"), "Should contain response field comment '动物种类'");

        // 验证 Object Array (listAnimals 返回 List<AnimalResp>)
        assertTrue(allContent.contains("Object Array"), "Should contain 'Object Array' for List return type");

        // 验证不匹配的 handler 不出现在文档中
        assertFalse(allContent.contains("创建动物"),
                "Should NOT contain '创建动物' (createAnimal does not match wildcards)");
        assertFalse(allContent.contains("删除动物"),
                "Should NOT contain '删除动物' (deleteAnimal does not match wildcards)");
        assertFalse(allContent.contains("POST"), "Should NOT contain HTTP method POST (createAnimal filtered)");
        assertFalse(allContent.contains("DELETE"), "Should NOT contain HTTP method DELETE (deleteAnimal filtered)");
    }

}

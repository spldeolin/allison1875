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
 * hierarchical-categories 集成测试。
 *
 * 验证 doc-analyzer 能正确处理 Javadoc 中的层级分类目录结构（如 api-docs/后台管理模块/系统设置.md）。
 *
 * @author Deolin 2025-05-13
 */
public class HierarchicalCategoriesItTest extends DocAnalyzerItBaseTest {

    @Test
    void test() throws IOException {
        runDocAnalyzer("hierarchical-categories");

        File apiDocsDir = new File(basedir, "api-docs");
        assertTrue(apiDocsDir.exists(), "api-docs directory should exist");

        // 验证层级目录结构：api-docs/后台管理模块/系统设置.md
        File hierarchicalDir = new File(apiDocsDir, "后台管理模块");
        assertTrue(hierarchicalDir.exists(), "Hierarchical category directory '后台管理模块' should exist");
        assertTrue(hierarchicalDir.isDirectory(), "'后台管理模块' should be a directory");

        // 验证精确的 md 文件
        File settingMd = new File(hierarchicalDir, "系统设置.md");
        assertTrue(settingMd.exists(), "系统设置.md should exist under '后台管理模块'");
        assertTrue(settingMd.isFile(), "系统设置.md should be a file");

        List<File> mdFiles = new ArrayList<>();
        Files.walkFileTree(hierarchicalDir.toPath(), new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                if (file.toString().endsWith(".md")) {
                    mdFiles.add(file.toFile());
                }
                return FileVisitResult.CONTINUE;
            }
        });
        assertEquals(1, mdFiles.size(), "Should generate exactly 1 .md file under hierarchical dir");

        StringBuilder sb = new StringBuilder();
        for (File mdFile : mdFiles) {
            sb.append(new String(Files.readAllBytes(mdFile.toPath()), StandardCharsets.UTF_8));
            sb.append("\n");
        }
        String allContent = sb.toString();

        assertTrue(allContent.contains("查询系统设置"), "Should contain '查询系统设置'");
        assertTrue(allContent.contains("GET /api/admin/settings"), "Should contain 'GET /api/admin/settings'");

        // 验证 Response Body 字段
        assertTrue(allContent.contains("key"), "Should contain response field 'key'");
        assertTrue(allContent.contains("设置键"), "Should contain response field comment '设置键'");
        assertTrue(allContent.contains("value"), "Should contain response field 'value'");
        assertTrue(allContent.contains("设置值"), "Should contain response field comment '设置值'");

        // 验证 api-docs 根目录下没有直接放置 md 文件（全部在子目录中）
        File[] rootFiles = apiDocsDir.listFiles((dir, name) -> name.endsWith(".md"));
        int rootMdCount = rootFiles != null ? rootFiles.length : 0;
        assertEquals(0, rootMdCount,
                "Root api-docs dir should NOT contain .md files directly, all should be in hierarchical subdirs");
    }

}

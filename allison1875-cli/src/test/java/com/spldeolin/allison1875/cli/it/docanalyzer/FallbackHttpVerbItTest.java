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
 * fallback-http-verb 集成测试。
 *
 * <p>验证裸 @RequestMapping（无 method 属性）时的 HTTP 方法渲染行为。
 * <p><b>设计决策：</b>当 combinedVerbs 为空时，仅回退为 POST，而非所有 HTTP 方法。
 * 原因是 POST 是最广泛通用的 HTTP 方法，裸 @RequestMapping 实际调用中通常使用 POST；
 * 此外，裸 @RequestMapping 本身也不是一种推荐的实现方式，一一列举所有方法意义不大。
 *
 * @author Deolin 2026-05-15
 */
public class FallbackHttpVerbItTest extends DocAnalyzerItBaseTest {

    @Test
    void test() throws IOException {
        runDocAnalyzer("fallback-http-verb");

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

        // 验证 md 文件名
        assertEquals(1, mdFiles.size(), "Should generate exactly 1 .md file");

        // 验证 handler
        assertTrue(allContent.contains("通用入口"), "Should contain '通用入口'");

        // 验证 URL
        assertTrue(allContent.contains("/api/fallback/entry"), "Should contain '/api/fallback/entry'");

        // 验证 HTTP 方法：裸 @RequestMapping 无 method 属性时，回退为 POST（设计特性，非BUG）
        // 原因：POST 是最广泛通用的 HTTP 方法，裸 @RequestMapping 通常配合 POST 使用；
        //       同时裸 @RequestMapping 不是推荐的实现方式，列举所有方法意义不大
        assertTrue(allContent.contains("POST"), "Should contain HTTP method POST (by design for bare @RequestMapping)");

        // 验证 Markdown 结构
        assertTrue(allContent.contains("### URL"), "Should contain '### URL' section");
    }

}

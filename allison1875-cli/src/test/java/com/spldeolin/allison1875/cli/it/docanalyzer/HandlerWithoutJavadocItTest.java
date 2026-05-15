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
 * handler-without-javadoc 集成测试。
 *
 * <p>验证 handler 方法没有 Javadoc 时，描述回退为 ControllerName.methodName 格式。
 *
 * @author Deolin 2026-05-15
 */
public class HandlerWithoutJavadocItTest extends DocAnalyzerItBaseTest {

    @Test
    void test() throws IOException {
        runDocAnalyzer("handler-without-javadoc");

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

        // 验证 handler 描述回退为 ControllerName.methodName 格式
        assertTrue(allContent.contains("NoJavadocController.getSomething"),
                "Should contain fallback description 'NoJavadocController.getSomething'");
        assertTrue(allContent.contains("NoJavadocController.postSomething"),
                "Should contain fallback description 'NoJavadocController.postSomething'");

        // 验证 URL
        assertTrue(allContent.contains("GET /api/nojavadoc/get"), "Should contain 'GET /api/nojavadoc/get'");
        assertTrue(allContent.contains("POST /api/nojavadoc/post"), "Should contain 'POST /api/nojavadoc/post'");

        // 验证 Markdown 结构
        assertTrue(allContent.contains("### URL"), "Should contain '### URL' section");
    }

}

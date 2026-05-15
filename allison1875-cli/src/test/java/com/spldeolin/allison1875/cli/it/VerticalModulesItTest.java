package com.spldeolin.allison1875.cli.it;

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
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

/**
 * vertical-modules 集成测试。
 * 垂直划分：controller 在当前模块，DTO 在 dto-api 子模块。
 * 验证 controllerModule 和 dtoModule 配置能正确解析不同目录的 sourceRoot，
 * AstForest 能跨多个 sourceRoot 解析 controller 和 DTO 的 AST。
 *
 * @author Deolin 2026-05-13
 */
public class VerticalModulesItTest extends DocAnalyzerItBaseTest {

    @Test
    void test() throws IOException {
        runDocAnalyzer("vertical-modules");

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

        // Controller from current module should be detected
        assertTrue(allContent.contains("创建商品"), "Should contain handler from controller module");
        assertTrue(allContent.contains("POST /api/products"), "Should contain 'POST /api/products'");

        // DTO fields from dto-api module should be resolved in documentation
        assertTrue(allContent.contains("productName"), "Should contain DTO field 'productName' from dto-api module");
        assertTrue(allContent.contains("price"), "Should contain DTO field 'price' from dto-api module");
        assertTrue(allContent.contains("description"), "Should contain DTO field 'description' from dto-api module");

        // Response Body fields
        assertTrue(allContent.contains("id"), "Should contain response field 'id'");

        // md filename
        List<String> fileNames = mdFiles.stream().map(File::getName).sorted().collect(Collectors.toList());
        assertTrue(fileNames.contains("商品管理.md"), "Should contain '商品管理.md'");
    }

}
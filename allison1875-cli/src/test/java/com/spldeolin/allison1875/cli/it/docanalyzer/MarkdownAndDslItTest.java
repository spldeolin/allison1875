package com.spldeolin.allison1875.cli.it.docanalyzer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * markdown-and-dsl 集成测试。
 *
 * <p>验证 doc-analyzer 同时以 MARKDOWN 和 DSL 模式输出：
 * api-docs/ 目录生成 .md 文件，api-dsls/ 目录生成 .json 文件。
 *
 * @author Deolin 2026-05-13
 */
public class MarkdownAndDslItTest extends DocAnalyzerItBaseTest {

    @Test
    void test() throws IOException {
        runDocAnalyzer("markdown-and-dsl");

        // ==================== 验证 MARKDOWN 输出 ====================

        File apiDocsDir = new File(basedir, "api-docs");
        assertTrue(apiDocsDir.exists(), "api-docs directory should exist");
        assertTrue(apiDocsDir.isDirectory(), "api-docs should be a directory");

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
        assertEquals("订单管理.md", mdFiles.get(0).getName(), "Markdown filename should be '订单管理.md'");

        // 验证5个 CRUD handler 的文档标题
        assertTrue(mdContent.contains("查询订单列表"), "Markdown should contain '查询订单列表'");
        assertTrue(mdContent.contains("支持按状态筛选"), "Markdown should contain sub-description '支持按状态筛选'");
        assertTrue(mdContent.contains("根据ID查询订单详情"), "Markdown should contain '根据ID查询订单详情'");
        assertTrue(mdContent.contains("创建订单"), "Markdown should contain '创建订单'");
        assertTrue(mdContent.contains("更新订单"), "Markdown should contain '更新订单'");
        assertTrue(mdContent.contains("删除订单"), "Markdown should contain '删除订单'");

        // 验证 URL（含 globalUrlPrefix: /v1）
        assertTrue(mdContent.contains("GET /v1/api/orders"), "Markdown should contain 'GET /v1/api/orders'");
        assertTrue(mdContent.contains("POST /v1/api/orders"), "Markdown should contain 'POST /v1/api/orders'");
        assertTrue(mdContent.contains("PUT /v1/api/orders/{orderId}"),
                "Markdown should contain 'PUT /v1/api/orders/{orderId}'");
        assertTrue(mdContent.contains("DELETE /v1/api/orders/{orderId}"),
                "Markdown should contain 'DELETE /v1/api/orders/{orderId}'");

        // 验证 PathVariable 出现在文档中
        assertTrue(mdContent.contains("orderId"), "Markdown should contain PathVariable 'orderId'");

        // 验证嵌套 DTO 的字段（OrderItemReq 中的 productId、quantity）
        assertTrue(mdContent.contains("productId"), "Markdown should contain nested field 'productId'");
        assertTrue(mdContent.contains("quantity"), "Markdown should contain nested field 'quantity'");

        // 验证请求体字段
        assertTrue(mdContent.contains("shippingAddress"), "Markdown should contain field 'shippingAddress'");
        assertTrue(mdContent.contains("buyerNote"), "Markdown should contain field 'buyerNote'");

        // 验证响应体嵌套字段
        assertTrue(mdContent.contains("productName"), "Markdown should contain nested response field 'productName'");
        assertTrue(mdContent.contains("unitPrice"), "Markdown should contain nested response field 'unitPrice'");

        // ==================== 验证 DSL 输出 ====================

        File apiDslsDir = new File(basedir, "api-dsls");
        assertTrue(apiDslsDir.exists(), "api-dsls directory should exist");
        assertTrue(apiDslsDir.isDirectory(), "api-dsls should be a directory");

        List<File> jsonFiles = new ArrayList<>();
        Files.walkFileTree(apiDslsDir.toPath(), new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                if (file.toString().endsWith(".json")) {
                    jsonFiles.add(file.toFile());
                }
                return FileVisitResult.CONTINUE;
            }
        });
        assertTrue(jsonFiles.size() > 0, "At least one .json file should be generated");

        // 验证 JSON 合法且包含 endpoint 信息
        ObjectMapper mapper = new ObjectMapper();
        int totalEndpoints = 0;
        for (File jsonFile : jsonFiles) {
            String jsonContent = new String(Files.readAllBytes(jsonFile.toPath()), StandardCharsets.UTF_8);
            JsonNode endpoints = mapper.readTree(jsonContent);
            assertNotNull(endpoints, "JSON should be parseable: " + jsonFile.getName());
            assertTrue(endpoints.isArray(), "JSON root should be an array: " + jsonFile.getName());
            totalEndpoints += endpoints.size();
        }

        // CRUD 场景应该有5个endpoint
        assertEquals(5, totalEndpoints, "Should have 5 endpoints in total (CRUD)");
    }

}

package com.spldeolin.allison1875.cli.it;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
 * basic-dsl 集成测试。
 *
 * <p>验证 doc-analyzer 以 DSL 模式输出后，api-dsls/ 目录下生成了正确的 .json 文件，
 * 包含商品管理的 GET 和 POST 两个 endpoint 的完整 DSL 结构。
 *
 * @author Deolin 2026-05-13
 */
public class BasicDslItTest extends DocAnalyzerItBaseTest {

    @Test
    void test() throws IOException {
        runDocAnalyzer("basic-dsl");

        // 验证 api-dsls 目录存在
        File apiDslsDir = new File(basedir, "api-dsls");
        assertTrue(apiDslsDir.exists(), "api-dsls directory should exist");
        assertTrue(apiDslsDir.isDirectory(), "api-dsls should be a directory");

        // 查找所有 json 文件
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

        // 验证 JSON 文件名和数量
        assertEquals(1, jsonFiles.size(), "Should generate exactly 1 .json file, but found " + jsonFiles.size());
        assertEquals("商品管理.json", jsonFiles.get(0).getName(), "JSON filename should be '商品管理.json'");

        // 解析 JSON
        ObjectMapper mapper = new ObjectMapper();
        String jsonContent = new String(Files.readAllBytes(jsonFiles.get(0).toPath()), StandardCharsets.UTF_8);
        JsonNode endpoints = mapper.readTree(jsonContent);
        assertNotNull(endpoints, "JSON should be parseable");
        assertTrue(endpoints.isArray(), "JSON root should be an array");

        // 验证 endpoint 数量（GET + POST = 2）
        assertEquals(2, endpoints.size(), "Should have 2 endpoints (GET + POST), but found " + endpoints.size());

        // 遍历验证 URL 和 HTTP 方法
        boolean foundProductUrl = false;
        boolean foundGetMethod = false;
        boolean foundPostMethod = false;
        JsonNode postEndpoint = null;
        JsonNode getEndpoint = null;

        for (JsonNode endpoint : endpoints) {
            JsonNode urls = endpoint.get("urls");
            if (urls != null) {
                for (JsonNode url : urls) {
                    if (url.asText().contains("/api/products")) {
                        foundProductUrl = true;
                    }
                }
            }
            String httpMethod = endpoint.has("httpMethod") ? endpoint.get("httpMethod").asText() : "";
            if ("get".equals(httpMethod)) {
                foundGetMethod = true;
                getEndpoint = endpoint;
            }
            if ("post".equals(httpMethod)) {
                foundPostMethod = true;
                postEndpoint = endpoint;
            }
        }

        assertTrue(foundProductUrl, "DSL output should contain URL /api/products");
        assertTrue(foundGetMethod, "DSL output should contain GET method");
        assertTrue(foundPostMethod, "DSL output should contain POST method");

        // 验证 POST endpoint DSL 结构
        assertNotNull(postEndpoint, "Should have a POST endpoint");
        JsonNode postDescLines = postEndpoint.get("descriptionLines");
        assertNotNull(postDescLines, "POST endpoint should have descriptionLines");
        boolean containsCreateProduct = false;
        for (JsonNode line : postDescLines) {
            if (line.asText().contains("创建商品")) {
                containsCreateProduct = true;
            }
        }
        assertTrue(containsCreateProduct, "POST endpoint description should contain '创建商品'");
        assertEquals("com.example.dto.req.CreateProductReq", postEndpoint.get("requestBodyDescribe").asText(),
                "POST endpoint should reference CreateProductReq");
        assertNotNull(postEndpoint.get("requestBodyJsonSchema"), "POST endpoint should have requestBodyJsonSchema");
        assertEquals("com.example.dto.resp.ProductResp", postEndpoint.get("responseBodyDescribe").asText(),
                "POST endpoint should reference ProductResp");

        // 验证 GET endpoint DSL 结构
        assertNotNull(getEndpoint, "Should have a GET endpoint");
        JsonNode getDescLines = getEndpoint.get("descriptionLines");
        assertNotNull(getDescLines, "GET endpoint should have descriptionLines");
        boolean containsQueryProduct = false;
        for (JsonNode line : getDescLines) {
            if (line.asText().contains("根据ID查询商品")) {
                containsQueryProduct = true;
            }
        }
        assertTrue(containsQueryProduct, "GET endpoint description should contain '根据ID查询商品'");
        JsonNode pathParams = getEndpoint.get("pathParams");
        assertNotNull(pathParams, "GET endpoint should have pathParams");
        assertEquals(1, pathParams.size(), "GET endpoint should have 1 path param");
        assertEquals("id", pathParams.get(0).get("name").asText(), "GET endpoint path param name should be 'id'");

        // 确保不会生成 markdown 目录（仅 DSL 模式）
        File apiDocsDir = new File(basedir, "api-docs");
        assertFalse(apiDocsDir.exists(), "api-docs directory should NOT exist in DSL-only mode");
    }

}

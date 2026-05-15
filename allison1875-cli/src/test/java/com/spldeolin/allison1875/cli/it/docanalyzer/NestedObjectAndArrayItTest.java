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
 * nested-object-and-array 集成测试。
 *
 * <p>验证 doc-analyzer 对多层嵌套 Object 和 Object Array 的 Markdown 渲染，
 * 包括嵌套字段缩进层级和共享 DTO 引用路径（「数据结构同 XXX」）。
 *
 * @author Deolin 2026-05-15
 */
public class NestedObjectAndArrayItTest extends DocAnalyzerItBaseTest {

    @Test
    void test() throws IOException {
        runDocAnalyzer("nested-object-and-array");

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
        String allContent = sb.toString();

        // 验证 md 文件名和数量
        assertEquals(1, mdFiles.size(), "Should generate exactly 1 .md file");
        assertEquals("订单管理.md", mdFiles.get(0).getName(), "Markdown filename should be '订单管理.md'");

        // 验证 handler
        assertTrue(allContent.contains("创建订单"), "Should contain '创建订单'");
        assertTrue(allContent.contains("POST /api/orders"), "Should contain 'POST /api/orders'");

        // 验证 Request Body 顶层字段
        assertTrue(allContent.contains("customer"), "Should contain field 'customer'");
        assertTrue(allContent.contains("客户信息"), "Should contain field comment '客户信息'");
        assertTrue(allContent.contains("items"), "Should contain field 'items'");
        assertTrue(allContent.contains("订单明细列表"), "Should contain field comment '订单明细列表'");
        assertTrue(allContent.contains("shippingAddress"), "Should contain field 'shippingAddress'");
        assertTrue(allContent.contains("收货地址"), "Should contain field comment '收货地址'");

        // 验证第2层嵌套字段（customer 下的 name, phone, contactAddress）
        assertTrue(allContent.contains("name"), "Should contain nested field 'name'");
        assertTrue(allContent.contains("客户姓名"), "Should contain nested field comment '客户姓名'");
        assertTrue(allContent.contains("phone"), "Should contain nested field 'phone'");
        assertTrue(allContent.contains("联系电话"), "Should contain nested field comment '联系电话'");

        // 验证第2层嵌套字段（items 下的 productName, quantity, detail）
        assertTrue(allContent.contains("productName"), "Should contain nested field 'productName'");
        assertTrue(allContent.contains("商品名称"), "Should contain nested field comment '商品名称'");
        assertTrue(allContent.contains("quantity"), "Should contain nested field 'quantity'");

        // 验证第3层嵌套字段（detail 下的 sku, warehouse）
        assertTrue(allContent.contains("sku"), "Should contain nested field 'sku'");
        assertTrue(allContent.contains("SKU编码"), "Should contain nested field comment 'SKU编码'");
        assertTrue(allContent.contains("warehouse"), "Should contain nested field 'warehouse'");
        assertTrue(allContent.contains("仓库名称"), "Should contain nested field comment '仓库名称'");

        // 验证第3层嵌套字段（contactAddress 下的 province, city, street）
        assertTrue(allContent.contains("province"), "Should contain nested field 'province'");
        assertTrue(allContent.contains("省份"), "Should contain nested field comment '省份'");
        assertTrue(allContent.contains("city"), "Should contain nested field 'city'");
        assertTrue(allContent.contains("城市"), "Should contain nested field comment '城市'");
        assertTrue(allContent.contains("street"), "Should contain nested field 'street'");
        assertTrue(allContent.contains("街道"), "Should contain nested field comment '街道'");

        // 验证 Response Body 顶层字段
        assertTrue(allContent.contains("orderId"), "Should contain response field 'orderId'");
        assertTrue(allContent.contains("订单ID"), "Should contain response field comment '订单ID'");

        // 验证 Markdown 结构标记
        assertTrue(allContent.contains("### Request Body (application/json)"), "Should contain request body section");
        assertTrue(allContent.contains("### Response Body (application/json)"), "Should contain response body section");

        // 验证嵌套字段使用 "- " 前缀进行层级缩进
        assertTrue(allContent.contains("- "), "Should contain '- ' prefix for nested field indentation");

        // 验证「数据结构同」引用路径
        assertTrue(allContent.contains("数据结构同"), "Should contain '数据结构同' reference text");
        assertTrue(allContent.contains("customer.contactAddress"),
                "Should contain 'customer.contactAddress' reference path");
    }

}

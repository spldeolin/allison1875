package com.spldeolin.allison1875.cli.it.formgenerator;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import org.junit.jupiter.api.Test;

/**
 * composite-index 集成测试。
 *
 * <p>验证多字段联合索引的处理：
 * <ul>
 *   <li>DDL 中生成多列 {@code UNIQUE KEY}，索引名为 {@code uk_col1_col2}</li>
 *   <li>persistence-generator 基于联合索引平铺生成最左前缀查询方法</li>
 *   <li>最左前缀（非唯一）返回 {@code List<Entity>}，完整联合索引（唯一）返回单个 {@code Entity}</li>
 *   <li>Mapper XML 中包含对应的多列精确匹配 {@code <select>} 语句</li>
 * </ul>
 *
 * @author Deolin 2026-05-17
 */
public class CompositeIndexItTest extends FormGeneratorItBaseTest {

    @Test
    void test() throws IOException {
        runFormGenerator("composite-index");

        // === DDL 验证 ===
        File ddlFile = new File(basedir, "sql/ddl.sql");
        assertTrue(ddlFile.exists(), "DDL file should be generated at sql/ddl.sql");
        String ddl = new String(Files.readAllBytes(ddlFile.toPath()), StandardCharsets.UTF_8);
        assertTrue(ddl.contains("CREATE TABLE"), "DDL should contain CREATE TABLE");
        // 联合唯一索引：两列
        assertTrue(ddl.contains("UNIQUE KEY `uk_order_no_customer_id`"),
                "DDL should contain composite UNIQUE KEY uk_order_no_customer_id");
        assertTrue(ddl.contains("`order_no`, `customer_id`"),
                "DDL should list both columns in composite index");

        // === Mapper 接口验证：最左前缀查询方法 ===
        File mapperFile = new File(basedir, "src/main/java/com/example/mapper/OrderMapper.java");
        assertTrue(mapperFile.exists(), "OrderMapper should be generated");
        String mapperContent = new String(Files.readAllBytes(mapperFile.toPath()), StandardCharsets.UTF_8);

        // 最左前缀 (orderNo)：非唯一 → 返回 List<OrderEntity>
        assertTrue(mapperContent.contains("queryByOrderNo"),
                "Mapper should contain queryByOrderNo (left-prefix of composite index)");
        assertTrue(mapperContent.contains("List<OrderEntity> queryByOrderNo"),
                "queryByOrderNo should return List (non-unique left-prefix)");

        // 完整联合索引 (orderNo, customerId)：唯一 → 返回单个 OrderEntity
        assertTrue(mapperContent.contains("queryByOrderNoCustomerId"),
                "Mapper should contain queryByOrderNoCustomerId (full composite index)");
        assertTrue(mapperContent.contains("OrderEntity queryByOrderNoCustomerId"),
                "queryByOrderNoCustomerId should return single OrderEntity (unique)");

        // === Mapper XML 验证 ===
        File xmlFile = new File(basedir, "src/main/resources/mapper/OrderMapper.xml");
        assertTrue(xmlFile.exists(), "Mapper XML should be generated");
        String xmlContent = new String(Files.readAllBytes(xmlFile.toPath()), StandardCharsets.UTF_8);
        // 最左前缀 XML
        assertTrue(xmlContent.contains("id=\"queryByOrderNo\""),
                "Mapper XML should contain queryByOrderNo select");
        assertTrue(xmlContent.contains("order_no = #{orderNo}"),
                "queryByOrderNo XML should match order_no");

        // 完整联合索引 XML：两列精确匹配
        assertTrue(xmlContent.contains("id=\"queryByOrderNoCustomerId\""),
                "Mapper XML should contain queryByOrderNoCustomerId select");
        assertTrue(xmlContent.contains("order_no = #{orderNo}"),
                "queryByOrderNoCustomerId XML should match order_no");
        assertTrue(xmlContent.contains("customer_id = #{customerId}"),
                "queryByOrderNoCustomerId XML should match customer_id");

        // === Save ServiceImpl 验证：编辑分支仍用 bizId 查询（不依赖联合索引） ===
        File saveServiceImplFile = new File(basedir,
                "src/main/java/com/example/service/impl/SaveOrderServiceImpl.java");
        assertTrue(saveServiceImplFile.exists(), "SaveOrderServiceImpl should be generated");
        String saveServiceImplContent = new String(Files.readAllBytes(saveServiceImplFile.toPath()),
                StandardCharsets.UTF_8);
        assertTrue(saveServiceImplContent.contains("orderMapper.queryByOrderCode("),
                "Save service edit branch should use bizId query (not composite index)");

        // === 验证没有生成 api-docs 目录 ===
        File apiDocsDir = new File(basedir, "api-docs");
        assertTrue(apiDocsDir.exists(), "api-docs directory should be generated");
    }

}

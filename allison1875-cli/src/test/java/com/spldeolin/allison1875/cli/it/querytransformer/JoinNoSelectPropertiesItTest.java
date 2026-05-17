package com.spldeolin.allison1875.cli.it.querytransformer;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import org.junit.jupiter.api.Test;

/**
 * query-transformer join-no-select-properties 集成测试。
 *
 * <p>验证 JOIN + 不指定主表 select 属性时，生成主表全列（t1.前缀+AS别名）和 join 表列：
 * 1. XML SELECT 子句包含所有主表列（t1.column_name AS fieldName）
 * 2. XML 包含 join 表的列
 * 3. t1/t2 别名正确
 *
 * @author Deolin 2026-05-17
 */
public class JoinNoSelectPropertiesItTest extends QueryTransformerItBaseTest {

    @Test
    void test() throws IOException {
        runQueryTransformer("join-no-select-properties");

        // ========== 1. Mapper XML 验证 ==========
        File mapperXmlFile = new File(basedir, "src/main/resources/mapper/TOrderMapper.xml");
        assertTrue(mapperXmlFile.exists(), "TOrderMapper.xml should exist");
        String xmlContent = new String(Files.readAllBytes(mapperXmlFile.toPath()), StandardCharsets.UTF_8);

        assertTrue(xmlContent.contains("id='listAllColumns'") || xmlContent.contains(
                "id=\"listAllColumns\""), "XML should contain listAllColumns select");
        String selectSection = extractSelectSection(xmlContent, "listAllColumns");

        // 主表所有列带 t1. 前缀 + AS 别名
        assertTrue(selectSection.contains("t1.id AS id"), "Should have t1.id AS id");
        assertTrue(selectSection.contains("t1.order_no AS orderNo"), "Should have t1.order_no AS orderNo");
        assertTrue(selectSection.contains("t1.user_id AS userId"), "Should have t1.user_id AS userId");
        assertTrue(selectSection.contains("t1.amount AS amount"), "Should have t1.amount AS amount");
        assertTrue(selectSection.contains("t1.status AS status"), "Should have t1.status AS status");
        assertTrue(selectSection.contains("t1.created_at AS createdAt"), "Should have t1.created_at AS createdAt");

        // t2 别名和 join 表
        assertTrue(selectSection.contains("t2."), "Should have t2 alias for joined table");
        assertTrue(selectSection.contains("LEFT JOIN"), "Should contain LEFT JOIN");

        // ========== 2. Mapper 接口验证 ==========
        File mapperFile = new File(basedir, "src/main/java/com/example/mapper/TOrderMapper.java");
        assertTrue(mapperFile.exists(), "TOrderMapper.java should exist");
        String mapperContent = new String(Files.readAllBytes(mapperFile.toPath()), StandardCharsets.UTF_8);
        assertTrue(mapperContent.contains("listAllColumns"), "Mapper should contain listAllColumns method");

        // ========== 3. Record DTO 验证 ==========
        File recordDir = new File(basedir, "src/main/java/com/example/dto/record");
        assertTrue(recordDir.exists(), "Record DTO directory should exist");
        File[] recordFiles = recordDir.listFiles();
        assertTrue(recordFiles != null && recordFiles.length > 0, "At least one Record DTO should be generated");

        // ========== 4. Service 文件验证 ==========
        File serviceFile = new File(basedir, "src/main/java/com/example/service/OrderService.java");
        assertTrue(serviceFile.exists(), "OrderService.java should exist");
        String serviceContent = new String(Files.readAllBytes(serviceFile.toPath()), StandardCharsets.UTF_8);

        assertFalse(serviceContent.contains("TOrderDesign."), "Design chain should be replaced");
        assertTrue(serviceContent.contains("TOrderMapper"), "Mapper should be injected");
        assertTrue(serviceContent.contains("tOrderMapper"), "Mapper field should exist");
    }

    private String extractSelectSection(String xml, String idValue) {
        int start = xml.indexOf("id='" + idValue + "'");
        if (start == -1) {
            start = xml.indexOf("id=\"" + idValue + "\"");
        }
        if (start == -1) {
            return "";
        }
        int end = xml.indexOf("</select>", start);
        return end > start ? xml.substring(start, end) : xml.substring(start);
    }

}
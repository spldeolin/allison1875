package com.spldeolin.allison1875.cli.it.querytransformer;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import org.junit.jupiter.api.Test;

/**
 * query-transformer join-multi-on-conditions 集成测试。
 *
 * <p>验证 JOIN 查询支持多个 ON 条件：
 * 1. DSL 中 JOIN 使用多个 ON 条件（如 .on().a.eq(TDesign.x).b.eq(TDesign.y)）
 * 2. XML ON 子句使用括号包裹多个条件，每个条件占一行
 *
 * @author Deolin 2026-05-17
 */
public class JoinMultiOnConditionsItTest extends QueryTransformerItBaseTest {

    @Test
    void test() throws IOException {
        runQueryTransformer("join-multi-on-conditions");

        // ========== 1. Mapper XML 验证 ==========
        File mapperXmlFile = new File(basedir, "src/main/resources/mapper/TOrderMapper.xml");
        assertTrue(mapperXmlFile.exists(), "TOrderMapper.xml should exist");
        String xmlContent = new String(Files.readAllBytes(mapperXmlFile.toPath()), StandardCharsets.UTF_8);

        // listByMultiOn: 多 ON 条件 LEFT JOIN
        assertTrue(xmlContent.contains("id='listByMultiOn'") || xmlContent.contains(
                "id=\"listByMultiOn\""), "XML should contain listByMultiOn select");

        // 提取 listByMultiOn 片段来断言多 ON 条件格式
        String multiOnSection = extractSelectSection(xmlContent, "listByMultiOn");

        // 多 ON 条件触发括号包裹
        assertTrue(multiOnSection.contains("ON ("), "Multi ON should have 'ON (' with parentheses");

        // ON 条件中包含主表别名 t1. 和被 join 表别名 t2.
        assertTrue(multiOnSection.contains("t1."), "ON should reference t1 alias for main table");
        assertTrue(multiOnSection.contains("t2."), "ON should reference t2 alias for joined table");

        // ON 条件包含 id 和 user_id 的关联（t2.id = t1.user_id）
        assertTrue(multiOnSection.contains("id") && multiOnSection.contains("user_id"),
                "ON should reference id and user_id columns");

        // ========== 2. Mapper 接口验证 ==========
        File mapperFile = new File(basedir, "src/main/java/com/example/mapper/TOrderMapper.java");
        assertTrue(mapperFile.exists(), "TOrderMapper.java should exist");
        String mapperContent = new String(Files.readAllBytes(mapperFile.toPath()), StandardCharsets.UTF_8);

        assertTrue(mapperContent.contains("listByMultiOn"), "Mapper should contain listByMultiOn method");

        // ========== 3. Service 文件验证 ==========
        File serviceFile = new File(basedir, "src/main/java/com/example/service/OrderService.java");
        assertTrue(serviceFile.exists(), "OrderService.java should exist");
        String serviceContent = new String(Files.readAllBytes(serviceFile.toPath()), StandardCharsets.UTF_8);

        assertFalse(serviceContent.contains("TOrderDesign.select"), "Design chain should be replaced");
        assertTrue(serviceContent.contains("TOrderMapper"), "Mapper should be injected");
        assertTrue(serviceContent.contains("tOrderMapper"), "Mapper field should exist");
    }

    /**
     * 从 XML 中提取某个 SELECT 语句的完整片段
     */
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
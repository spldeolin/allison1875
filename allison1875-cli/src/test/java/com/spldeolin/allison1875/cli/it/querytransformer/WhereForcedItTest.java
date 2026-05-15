package com.spldeolin.allison1875.cli.it.querytransformer;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import org.junit.jupiter.api.Test;

/**
 * query-transformer where-forced 集成测试。
 *
 * <p>验证 .whereEvenNull() 强制条件模式：
 * 1. XML 中条件不包裹 &lt;if test&gt;
 * 2. 直接输出 AND column = #{var}
 * 3. 适用于 SELECT 和 DELETE
 *
 * @author Deolin 2026-05-15
 */
public class WhereForcedItTest extends QueryTransformerItBaseTest {

    @Test
    void test() throws IOException {
        runQueryTransformer("where-forced");

        // ========== 1. Mapper XML 验证 ==========
        File mapperXmlFile = new File(basedir, "src/main/resources/mapper/TOrderMapper.xml");
        assertTrue(mapperXmlFile.exists(), "TOrderMapper.xml should exist");
        String xmlContent = new String(Files.readAllBytes(mapperXmlFile.toPath()), StandardCharsets.UTF_8);

        // findByIdForced: 强制 eq，不应有 <if test>
        assertTrue(xmlContent.contains("id='findByIdForced'") || xmlContent.contains("id=\"findByIdForced\""),
                "XML should contain findByIdForced");
        // 提取 findByIdForced 区域
        String findByIdSection = extractSelectSection(xmlContent, "findByIdForced");
        assertFalse(findByIdSection.contains("<if test"), "Forced mode should NOT have <if test> wrapping");
        assertTrue(findByIdSection.contains("AND id = #{id}"), "Forced mode should have direct 'AND id = #{id}'");

        // listByStatusAndUserIdForced: 强制多条件
        assertTrue(xmlContent.contains("id='listByStatusAndUserIdForced'") || xmlContent.contains(
                "id=\"listByStatusAndUserIdForced\""), "XML should contain listByStatusAndUserIdForced");
        String listForcedSection = extractSelectSection(xmlContent, "listByStatusAndUserIdForced");
        assertFalse(listForcedSection.contains("<if test"), "Forced multi-condition should NOT have <if test>");
        assertTrue(listForcedSection.contains("AND status = #{status}"), "Should have direct 'AND status = #{status}'");
        assertTrue(listForcedSection.contains("AND user_id = #{userId}"),
                "Should have direct 'AND user_id = #{userId}'");

        // deleteByIdForced: 强制 DELETE
        assertTrue(xmlContent.contains("id='deleteByIdForced'") || xmlContent.contains("id=\"deleteByIdForced\""),
                "XML should contain deleteByIdForced");
        String deleteSection = extractDeleteSection(xmlContent, "deleteByIdForced");
        assertFalse(deleteSection.contains("<if test"), "Forced DELETE should NOT have <if test>");
        assertTrue(deleteSection.contains("AND id = #{id}"), "Forced DELETE should have direct 'AND id = #{id}'");

        // ========== 2. Mapper 接口验证 ==========
        File mapperFile = new File(basedir, "src/main/java/com/example/mapper/TOrderMapper.java");
        assertTrue(mapperFile.exists(), "TOrderMapper.java should exist");
        String mapperContent = new String(Files.readAllBytes(mapperFile.toPath()), StandardCharsets.UTF_8);

        assertTrue(mapperContent.contains("findByIdForced"), "Mapper should contain findByIdForced");
        assertTrue(mapperContent.contains("listByStatusAndUserIdForced"),
                "Mapper should contain listByStatusAndUserIdForced");
        assertTrue(mapperContent.contains("deleteByIdForced"), "Mapper should contain deleteByIdForced");

        // ========== 3. Service 文件验证 ==========
        File serviceFile = new File(basedir, "src/main/java/com/example/service/OrderService.java");
        assertTrue(serviceFile.exists(), "OrderService.java should exist");
        String serviceContent = new String(Files.readAllBytes(serviceFile.toPath()), StandardCharsets.UTF_8);

        // Design 链已被替换
        assertFalse(serviceContent.contains("TOrderDesign."), "Design chain should be replaced");

        // Mapper 注入和方法调用
        assertTrue(serviceContent.contains("TOrderMapper"), "Mapper should be injected");
        assertTrue(serviceContent.contains("tOrderMapper"), "Mapper field should exist");
        assertTrue(serviceContent.contains("tOrderMapper.findByIdForced"), "Should call mapper.findByIdForced");
        assertTrue(serviceContent.contains("tOrderMapper.listByStatusAndUserIdForced"),
                "Should call mapper.listByStatusAndUserIdForced");
        assertTrue(serviceContent.contains("tOrderMapper.deleteByIdForced"), "Should call mapper.deleteByIdForced");
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

    /**
     * 从 XML 中提取某个 DELETE 语句的完整片段
     */
    private String extractDeleteSection(String xml, String idValue) {
        int start = xml.indexOf("id='" + idValue + "'");
        if (start == -1) {
            start = xml.indexOf("id=\"" + idValue + "\"");
        }
        if (start == -1) {
            return "";
        }
        int end = xml.indexOf("</delete>", start);
        return end > start ? xml.substring(start, end) : xml.substring(start);
    }

}
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
 * 4. 全部比较运算符（eq/ne/gt/ge/lt/le/like/in/nin）
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

        // ---- whereEvenNull + ne: != ----
        assertTrue(xmlContent.contains("id='queryByNeForced'") || xmlContent.contains("id=\"queryByNeForced\""),
                "XML should contain queryByNeForced");
        String neSection = extractSelectSection(xmlContent, "queryByNeForced");
        assertFalse(neSection.contains("<if test"), "Forced ne should NOT have <if test>");
        assertTrue(neSection.contains("status != #{status}"), "Forced ne should generate 'status != #{status}'");

        // ---- whereEvenNull + gt: > ----
        assertTrue(xmlContent.contains("id='queryByGtForced'") || xmlContent.contains("id=\"queryByGtForced\""),
                "XML should contain queryByGtForced");
        String gtSection = extractSelectSection(xmlContent, "queryByGtForced");
        assertFalse(gtSection.contains("<if test"), "Forced gt should NOT have <if test>");
        assertTrue(gtSection.contains("amount > #{amount}")
                        || gtSection.contains("amount > #{minAmount}"),
                "Forced gt should generate 'amount > #{var}'");

        // ---- whereEvenNull + ge: >= ----
        assertTrue(xmlContent.contains("id='queryByGeForced'") || xmlContent.contains("id=\"queryByGeForced\""),
                "XML should contain queryByGeForced");
        String geSection = extractSelectSection(xmlContent, "queryByGeForced");
        assertFalse(geSection.contains("<if test"), "Forced ge should NOT have <if test>");
        assertTrue(geSection.contains("amount >= #{amount}")
                        || geSection.contains("amount >= #{minAmount}"),
                "Forced ge should generate 'amount >= #{var}'");

        // ---- whereEvenNull + lt: < (XML escaped) ----
        assertTrue(xmlContent.contains("id='queryByLtForced'") || xmlContent.contains("id=\"queryByLtForced\""),
                "XML should contain queryByLtForced");
        String ltSection = extractSelectSection(xmlContent, "queryByLtForced");
        assertFalse(ltSection.contains("<if test"), "Forced lt should NOT have <if test>");
        assertTrue(ltSection.contains("amount &lt; #{amount}")
                        || ltSection.contains("amount &lt; #{maxAmount}"),
                "Forced lt should generate 'amount < #{var}' (XML escaped)");

        // ---- whereEvenNull + le: <= (XML escaped) ----
        assertTrue(xmlContent.contains("id='queryByLeForced'") || xmlContent.contains("id=\"queryByLeForced\""),
                "XML should contain queryByLeForced");
        String leSection = extractSelectSection(xmlContent, "queryByLeForced");
        assertFalse(leSection.contains("<if test"), "Forced le should NOT have <if test>");
        assertTrue(leSection.contains("amount &lt;= #{amount}")
                        || leSection.contains("amount &lt;= #{maxAmount}"),
                "Forced le should generate 'amount <= #{var}' (XML escaped)");

        // ---- whereEvenNull + like: LIKE CONCAT ----
        assertTrue(xmlContent.contains("id='queryByLikeForced'") || xmlContent.contains("id=\"queryByLikeForced\""),
                "XML should contain queryByLikeForced");
        String likeSection = extractSelectSection(xmlContent, "queryByLikeForced");
        assertFalse(likeSection.contains("<if test"), "Forced like should NOT have <if test>");
        assertTrue(likeSection.contains("LIKE CONCAT('%',"),
                "Forced like should generate LIKE CONCAT pattern");

        // ---- whereEvenNull + in: IN (foreach) ----
        assertTrue(xmlContent.contains("id='queryByInForced'") || xmlContent.contains("id=\"queryByInForced\""),
                "XML should contain queryByInForced");
        String inSection = extractSelectSection(xmlContent, "queryByInForced");
        assertFalse(inSection.contains("<if test"), "Forced in should NOT have <if test>");
        assertTrue(inSection.contains("IN (") && inSection.contains("foreach"),
                "Forced in should generate 'IN (...)' with foreach");

        // ---- whereEvenNull + nin: NOT IN (foreach) ----
        assertTrue(xmlContent.contains("id='queryByNinForced'") || xmlContent.contains("id=\"queryByNinForced\""),
                "XML should contain queryByNinForced");
        String ninSection = extractSelectSection(xmlContent, "queryByNinForced");
        assertFalse(ninSection.contains("<if test"), "Forced nin should NOT have <if test>");
        assertTrue(ninSection.contains("NOT IN (") && ninSection.contains("foreach"),
                "Forced nin should generate 'NOT IN (...)' with foreach");

        // ========== 2. Mapper 接口验证 ==========
        File mapperFile = new File(basedir, "src/main/java/com/example/mapper/TOrderMapper.java");
        assertTrue(mapperFile.exists(), "TOrderMapper.java should exist");
        String mapperContent = new String(Files.readAllBytes(mapperFile.toPath()), StandardCharsets.UTF_8);

        assertTrue(mapperContent.contains("findByIdForced"), "Mapper should contain findByIdForced");
        assertTrue(mapperContent.contains("listByStatusAndUserIdForced"),
                "Mapper should contain listByStatusAndUserIdForced");
        assertTrue(mapperContent.contains("deleteByIdForced"), "Mapper should contain deleteByIdForced");
        assertTrue(mapperContent.contains("queryByNeForced"), "Mapper should contain queryByNeForced");
        assertTrue(mapperContent.contains("queryByGtForced"), "Mapper should contain queryByGtForced");
        assertTrue(mapperContent.contains("queryByGeForced"), "Mapper should contain queryByGeForced");
        assertTrue(mapperContent.contains("queryByLtForced"), "Mapper should contain queryByLtForced");
        assertTrue(mapperContent.contains("queryByLeForced"), "Mapper should contain queryByLeForced");
        assertTrue(mapperContent.contains("queryByLikeForced"), "Mapper should contain queryByLikeForced");
        assertTrue(mapperContent.contains("queryByInForced"), "Mapper should contain queryByInForced");
        assertTrue(mapperContent.contains("queryByNinForced"), "Mapper should contain queryByNinForced");

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
        assertTrue(serviceContent.contains("tOrderMapper.queryByNeForced"), "Should call mapper.queryByNeForced");
        assertTrue(serviceContent.contains("tOrderMapper.queryByGtForced"), "Should call mapper.queryByGtForced");
        assertTrue(serviceContent.contains("tOrderMapper.queryByGeForced"), "Should call mapper.queryByGeForced");
        assertTrue(serviceContent.contains("tOrderMapper.queryByLtForced"), "Should call mapper.queryByLtForced");
        assertTrue(serviceContent.contains("tOrderMapper.queryByLeForced"), "Should call mapper.queryByLeForced");
        assertTrue(serviceContent.contains("tOrderMapper.queryByLikeForced"), "Should call mapper.queryByLikeForced");
        assertTrue(serviceContent.contains("tOrderMapper.queryByInForced"), "Should call mapper.queryByInForced");
        assertTrue(serviceContent.contains("tOrderMapper.queryByNinForced"), "Should call mapper.queryByNinForced");
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
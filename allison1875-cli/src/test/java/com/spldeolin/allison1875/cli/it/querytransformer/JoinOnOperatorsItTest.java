package com.spldeolin.allison1875.cli.it.querytransformer;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import org.junit.jupiter.api.Test;

/**
 * query-transformer join-on-operators 集成测试。
 *
 * <p>验证 JOIN ON 条件支持 eq 以外的运算符：
 * 1. ne() → ON 子句出现 !=
 * 2. gt() → ON 子句出现 >
 * 3. in() → ON 子句出现 IN foreach
 *
 * @author Deolin 2026-05-17
 */
public class JoinOnOperatorsItTest extends QueryTransformerItBaseTest {

    @Test
    void test() throws IOException {
        runQueryTransformer("join-on-operators");

        // ========== 1. Mapper XML 验证 ==========
        File mapperXmlFile = new File(basedir, "src/main/resources/mapper/TOrderMapper.xml");
        assertTrue(mapperXmlFile.exists(), "TOrderMapper.xml should exist");
        String xmlContent = new String(Files.readAllBytes(mapperXmlFile.toPath()), StandardCharsets.UTF_8);

        // listByOnNe: ON !=
        assertTrue(xmlContent.contains("id='listByOnNe'") || xmlContent.contains(
                "id=\"listByOnNe\""), "XML should contain listByOnNe");
        String neSection = extractSelectSection(xmlContent, "listByOnNe");
        assertTrue(neSection.contains("LEFT JOIN"), "Should be LEFT JOIN");
        assertTrue(neSection.contains("!="), "ON ne should generate '!=' operator");

        // listByOnGt: ON >
        assertTrue(xmlContent.contains("id='listByOnGt'") || xmlContent.contains(
                "id=\"listByOnGt\""), "XML should contain listByOnGt");
        String gtSection = extractSelectSection(xmlContent, "listByOnGt");
        assertTrue(gtSection.contains("LEFT JOIN"), "Should be LEFT JOIN");
        assertTrue(gtSection.contains(" > "), "ON gt should generate '>' operator");

        // listByOnIn: ON IN (foreach)
        assertTrue(xmlContent.contains("id='listByOnIn'") || xmlContent.contains(
                "id=\"listByOnIn\""), "XML should contain listByOnIn");
        String inSection = extractSelectSection(xmlContent, "listByOnIn");
        assertTrue(inSection.contains("LEFT JOIN"), "Should be LEFT JOIN");
        assertTrue(inSection.contains("IN (") && inSection.contains("foreach"),
                "ON in should generate 'IN (foreach)'");

        // ========== 2. Mapper 接口验证 ==========
        File mapperFile = new File(basedir, "src/main/java/com/example/mapper/TOrderMapper.java");
        assertTrue(mapperFile.exists(), "TOrderMapper.java should exist");
        String mapperContent = new String(Files.readAllBytes(mapperFile.toPath()), StandardCharsets.UTF_8);

        assertTrue(mapperContent.contains("listByOnNe"), "Mapper should contain listByOnNe");
        assertTrue(mapperContent.contains("listByOnGt"), "Mapper should contain listByOnGt");
        assertTrue(mapperContent.contains("listByOnIn"), "Mapper should contain listByOnIn");

        // ========== 3. Service 文件验证 ==========
        File serviceFile = new File(basedir, "src/main/java/com/example/service/OrderService.java");
        assertTrue(serviceFile.exists(), "OrderService.java should exist");
        String serviceContent = new String(Files.readAllBytes(serviceFile.toPath()), StandardCharsets.UTF_8);

        assertFalse(serviceContent.contains("TOrderDesign."), "Design chain should be replaced");
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
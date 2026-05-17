package com.spldeolin.allison1875.cli.it.querytransformer;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import org.junit.jupiter.api.Test;

/**
 * @author Deolin 2026-05-17
 */
public class SelectWithOrderAndJoinItTest extends QueryTransformerItBaseTest {

    @Test
    void test() throws IOException {
        runQueryTransformer("select-with-order-and-join");

        File mapperXml = new File(basedir, "src/main/resources/mapper/TOrderMapper.xml");
        String xml = new String(Files.readAllBytes(mapperXml.toPath()), StandardCharsets.UTF_8);
        assertTrue(xml.contains("id='listOrderWithUserOrderById'") || xml.contains(
                "id=\"listOrderWithUserOrderById\""));

        String section = extractSelectSection(xml, "listOrderWithUserOrderById");
        assertTrue(section.contains("LEFT JOIN"), "Should contain LEFT JOIN");
        // ORDER BY 带 t1. 前缀
        assertTrue(section.contains("ORDER BY") && section.contains("t1.id"),
                "ORDER BY in join should have t1. prefix");
        // SELECT 混合主表和 join 表列
        assertTrue(section.contains("t1."), "Should have t1. prefix for main table columns");
        assertTrue(section.contains("t2."), "Should have t2. prefix for joined table columns");

        File serviceFile = new File(basedir, "src/main/java/com/example/service/OrderService.java");
        String serviceContent = new String(Files.readAllBytes(serviceFile.toPath()), StandardCharsets.UTF_8);
        assertFalse(serviceContent.contains("TOrderDesign."));
        assertTrue(serviceContent.contains("TOrderMapper"));
    }

    private String extractSelectSection(String xml, String idValue) {
        int start = xml.indexOf("id='" + idValue + "'");
        if (start == -1) start = xml.indexOf("id=\"" + idValue + "\"");
        if (start == -1) return "";
        int end = xml.indexOf("</select>", start);
        return end > start ? xml.substring(start, end) : xml.substring(start);
    }
}
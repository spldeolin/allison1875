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
public class DeleteWithInOperatorItTest extends QueryTransformerItBaseTest {

    @Test
    void test() throws IOException {
        runQueryTransformer("delete-with-in-operator");

        File mapperXml = new File(basedir, "src/main/resources/mapper/TOrderMapper.xml");
        String xml = new String(Files.readAllBytes(mapperXml.toPath()), StandardCharsets.UTF_8);
        assertTrue(xml.contains("id='deleteByIdIn'") || xml.contains("id=\"deleteByIdIn\""));

        // <delete> 标签不应有 parameterType
        String deleteSection = extractDeleteSection(xml, "deleteByIdIn");
        assertFalse(deleteSection.contains("parameterType"),
                "Delete with IN should NOT have parameterType attribute");
    }

    private String extractDeleteSection(String xml, String idValue) {
        int start = xml.indexOf("id='" + idValue + "'");
        if (start == -1) start = xml.indexOf("id=\"" + idValue + "\"");
        if (start == -1) return "";
        int end = xml.indexOf("</delete>", start);
        return end > start ? xml.substring(start, end) : xml.substring(start);
    }
}
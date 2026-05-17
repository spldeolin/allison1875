package com.spldeolin.allison1875.cli.it.querytransformer;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import org.junit.jupiter.api.Test;

/**
 * @author Deolin 2026-05-17
 */
public class SoftDeleteInQueryItTest extends QueryTransformerItBaseTest {

    @Test
    void test() throws IOException {
        runQueryTransformer("soft-delete-in-query");

        File mapperXml = new File(basedir, "src/main/resources/mapper/TOrderMapper.xml");
        String xml = new String(Files.readAllBytes(mapperXml.toPath()), StandardCharsets.UTF_8);
        // WHERE 自动包含 is_deleted = 0
        assertTrue(xml.contains("is_deleted = 0"), "XML WHERE should contain soft-delete condition is_deleted = 0");
    }
}
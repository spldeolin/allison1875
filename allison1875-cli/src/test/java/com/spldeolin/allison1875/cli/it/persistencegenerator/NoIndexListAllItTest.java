package com.spldeolin.allison1875.cli.it.persistencegenerator;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import org.junit.jupiter.api.Test;

/**
 * persistence-generator no-index-list-all 集成测试。
 *
 * @author Deolin 2026-05-14
 */
public class NoIndexListAllItTest extends PersistenceGeneratorItBaseTest {

    @Test
    void test() throws IOException {
        runPersistenceGenerator("no-index-list-all");

        File mapperFile = new File(basedir, "src/main/java/com/example/mapper/TConfigMapper.java");
        assertTrue(mapperFile.exists(), "TConfigMapper.java should be generated");
        String mapperContent = new String(Files.readAllBytes(mapperFile.toPath()), StandardCharsets.UTF_8);

        // 无索引时应该生成 listAll
        assertTrue(mapperContent.contains("listAll"), "Mapper should contain listAll for table without indexes");

        // 不应该有 queryByConfigKey（无索引）
        assertFalse(mapperContent.contains("queryByConfigKey"),
                "Should NOT have queryByConfigKey (no index on config_key)");

        File xmlFile = new File(basedir, "src/main/resources/mapper/TConfigMapper.xml");
        assertTrue(xmlFile.exists(), "TConfigMapper.xml should be generated");
        String xmlContent = new String(Files.readAllBytes(xmlFile.toPath()), StandardCharsets.UTF_8);
        assertTrue(xmlContent.contains("listAll"), "XML should contain listAll select");
        assertTrue(xmlContent.contains("SELECT <include refid=\"all\"/>"), "listAll should select all columns");
    }

}

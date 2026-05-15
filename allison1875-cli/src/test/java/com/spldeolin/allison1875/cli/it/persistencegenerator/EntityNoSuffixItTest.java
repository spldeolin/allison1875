package com.spldeolin.allison1875.cli.it.persistencegenerator;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import org.junit.jupiter.api.Test;

/**
 * persistence-generator entity-no-suffix 集成测试。
 *
 * @author Deolin 2026-05-14
 */
public class EntityNoSuffixItTest extends PersistenceGeneratorItBaseTest {

    @Test
    void test() throws IOException {
        runPersistenceGenerator("entity-no-suffix");

        File entityFile = new File(basedir, "src/main/java/com/example/entity/TCategory.java");
        assertTrue(entityFile.exists(), "TCategory.java should be generated (without Entity suffix)");

        File entityWithSuffix = new File(basedir, "src/main/java/com/example/entity/TCategoryEntity.java");
        assertFalse(entityWithSuffix.exists(),
                "TCategoryEntity.java should NOT exist when isEntityEndWithEntity=false");

        String entityContent = new String(Files.readAllBytes(entityFile.toPath()), StandardCharsets.UTF_8);
        assertTrue(entityContent.contains("class TCategory"), "Class should be named TCategory");
        assertTrue(entityContent.contains("package com.example.entity"), "Should have correct package");

        File mapperFile = new File(basedir, "src/main/java/com/example/mapper/TCategoryMapper.java");
        assertTrue(mapperFile.exists(), "TCategoryMapper.java should be generated");
        String mapperContent = new String(Files.readAllBytes(mapperFile.toPath()), StandardCharsets.UTF_8);
        assertTrue(mapperContent.contains("TCategory"), "Mapper should reference TCategory (not TCategoryEntity)");

        File xmlFile = new File(basedir, "src/main/resources/mapper/TCategoryMapper.xml");
        assertTrue(xmlFile.exists(), "TCategoryMapper.xml should be generated");
        String xmlContent = new String(Files.readAllBytes(xmlFile.toPath()), StandardCharsets.UTF_8);
        assertTrue(xmlContent.contains("com.example.entity.TCategory"),
                "XML resultMap should use com.example.entity.TCategory");
    }

}

package com.spldeolin.allison1875.cli.it.persistencegenerator;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import org.junit.jupiter.api.Test;

/**
 * persistence-generator longtext-column 集成测试。
 *
 * @author Deolin 2026-05-16
 */
public class LongtextColumnItTest extends PersistenceGeneratorItBaseTest {

    @Test
    void test() throws IOException {
        runPersistenceGenerator("longtext-column");

        File entityFile = new File(basedir, "src/main/java/com/example/entity/TArticleContentEntity.java");
        assertTrue(entityFile.exists(), "TArticleContentEntity.java should be generated");
        String entityContent = new String(Files.readAllBytes(entityFile.toPath()), StandardCharsets.UTF_8);
        assertTrue(entityContent.contains("String body"), "longtext column should map to String body");

        File xmlFile = new File(basedir, "src/main/resources/mapper/TArticleContentMapper.xml");
        assertTrue(xmlFile.exists(), "TArticleContentMapper.xml should be generated");
        String xmlContent = new String(Files.readAllBytes(xmlFile.toPath()), StandardCharsets.UTF_8);
        assertTrue(xmlContent.contains("body"), "XML should contain body column mapping");
    }

}

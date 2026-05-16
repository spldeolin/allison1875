package com.spldeolin.allison1875.cli.it.persistencegenerator;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import org.junit.jupiter.api.Test;

/**
 * persistence-generator unsupported-column-type 集成测试。
 *
 * @author Deolin 2026-05-16
 */
public class UnsupportedColumnTypeItTest extends PersistenceGeneratorItBaseTest {

    @Test
    void test() throws IOException {
        runPersistenceGenerator("unsupported-column-type");

        File entityFile = new File(basedir, "src/main/java/com/example/entity/TMixedColumnEntity.java");
        assertTrue(entityFile.exists(), "TMixedColumnEntity.java should be generated");
        String entityContent = new String(Files.readAllBytes(entityFile.toPath()), StandardCharsets.UTF_8);

        assertTrue(entityContent.contains("Long id"), "Entity should contain supported id field");
        assertTrue(entityContent.contains("String name"), "Entity should contain supported name field");
        assertFalse(entityContent.contains("payload"), "Unsupported blob column should be skipped");
    }

}

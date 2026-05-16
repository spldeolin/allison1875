package com.spldeolin.allison1875.cli.it.persistencegenerator;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import org.junit.jupiter.api.Test;

/**
 * persistence-generator entity-existence-rename 集成测试。
 *
 * @author Deolin 2026-05-16
 */
public class EntityExistenceRenameItTest extends PersistenceGeneratorItBaseTest {

    @Test
    void test() throws IOException {
        runPersistenceGenerator("entity-existence-rename");

        File legacyEntity = new File(basedir, "src/main/java/com/example/entity/TProductEntity.java");
        assertTrue(legacyEntity.exists(), "Original TProductEntity.java should remain");
        String legacyContent = new String(Files.readAllBytes(legacyEntity.toPath()), StandardCharsets.UTF_8);
        assertTrue(legacyContent.contains("legacyMarker"), "Original entity should keep legacy content");

        File generatedEntity = new File(basedir, "src/main/java/com/example/entity/TProductEntityEx.java");
        assertTrue(generatedEntity.exists(), "Generated entity should be written to TProductEntityEx.java");
        String generatedContent = new String(Files.readAllBytes(generatedEntity.toPath()), StandardCharsets.UTF_8);
        assertTrue(generatedContent.contains("productName"), "Generated entity should contain productName");
        assertFalse(generatedContent.contains("legacyMarker"), "Generated entity should not contain legacy marker");
    }

}

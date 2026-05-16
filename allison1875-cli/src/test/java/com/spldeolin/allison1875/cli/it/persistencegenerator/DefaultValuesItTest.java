package com.spldeolin.allison1875.cli.it.persistencegenerator;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import org.junit.jupiter.api.Test;

/**
 * persistence-generator default-values 集成测试。
 *
 * @author Deolin 2026-05-16
 */
public class DefaultValuesItTest extends PersistenceGeneratorItBaseTest {

    @Test
    void test() throws IOException {
        runPersistenceGenerator("default-values");

        File entityFile = new File(basedir, "src/main/java/com/example/entity/TDefaultValueEntity.java");
        assertTrue(entityFile.exists(), "TDefaultValueEntity.java should be generated");
        String entityContent = new String(Files.readAllBytes(entityFile.toPath()), StandardCharsets.UTF_8);

        assertTrue(entityContent.contains("Integer status = 0"), "status should init to 0");
        assertTrue(entityContent.contains("String name = \"\""), "name should init to empty string");
        assertTrue(entityContent.contains("Long version = 0"), "version should init to 0");
        assertTrue(entityContent.contains("Boolean enabled = false"), "enabled should init to false");
        assertTrue(entityContent.contains("BigDecimal amount = new BigDecimal(0.00)"),
                "amount should init via BigDecimal");
    }

}

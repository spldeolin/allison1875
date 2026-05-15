package com.spldeolin.allison1875.cli.it.persistencegenerator;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import org.junit.jupiter.api.Test;

/**
 * persistence-generator no-lombok 集成测试。
 *
 * @author Deolin 2026-05-14
 */
public class NoLombokItTest extends PersistenceGeneratorItBaseTest {

    @Test
    void test() throws IOException {
        runPersistenceGenerator("no-lombok");

        File entityFile = new File(basedir, "src/main/java/com/example/entity/TItemEntity.java");
        assertTrue(entityFile.exists(), "TItemEntity.java should be generated");
        String entityContent = new String(Files.readAllBytes(entityFile.toPath()), StandardCharsets.UTF_8);

        // 不应包含 Lombok 注解
        assertFalse(entityContent.contains("@Data"), "Entity should NOT have @Data annotation");
        assertFalse(entityContent.contains("@Accessors"), "Entity should NOT have @Accessors annotation");
        assertFalse(entityContent.contains("@FieldDefaults"), "Entity should NOT have @FieldDefaults annotation");

        // 应该有手写的 getter/setter 方法
        assertTrue(entityContent.contains("getId"), "Entity should have getId getter");
        assertTrue(entityContent.contains("setId"), "Entity should have setId setter");
        assertTrue(entityContent.contains("getItemName"), "Entity should have getItemName getter");
        assertTrue(entityContent.contains("setItemName"), "Entity should have setItemName setter");
        assertTrue(entityContent.contains("getQuantity"), "Entity should have getQuantity getter");
        assertTrue(entityContent.contains("setQuantity"), "Entity should have setQuantity setter");
    }

}

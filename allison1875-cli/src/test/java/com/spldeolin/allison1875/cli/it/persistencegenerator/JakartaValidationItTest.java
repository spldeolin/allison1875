package com.spldeolin.allison1875.cli.it.persistencegenerator;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import org.junit.jupiter.api.Test;

/**
 * persistence-generator jakarta-validation 集成测试。
 *
 * <p>验证 {@code enableJavaxMoveToJakarta=true} 时 persistence-generator 可正常完成生成，
 * 且生成代码中不包含 javax 命名空间（与 Jakarta 项目兼容）。
 *
 * @author Deolin 2026-05-16
 */
public class JakartaValidationItTest extends PersistenceGeneratorItBaseTest {

    @Test
    void test() throws IOException {
        runPersistenceGenerator("jakarta-validation");

        File entityFile = new File(basedir, "src/main/java/com/example/entity/TMemberEntity.java");
        assertTrue(entityFile.exists(), "TMemberEntity.java should be generated");
        String entityContent = new String(Files.readAllBytes(entityFile.toPath()), StandardCharsets.UTF_8);
        assertTrue(entityContent.contains("memberName"), "Entity should contain memberName field");
        assertFalse(entityContent.contains("javax."), "Generated entity should not use javax namespace");

        File mapperFile = new File(basedir, "src/main/java/com/example/mapper/TMemberMapper.java");
        assertTrue(mapperFile.exists(), "TMemberMapper.java should be generated");
        String mapperContent = new String(Files.readAllBytes(mapperFile.toPath()), StandardCharsets.UTF_8);
        assertFalse(mapperContent.contains("javax."), "Generated mapper should not use javax namespace");
    }

}

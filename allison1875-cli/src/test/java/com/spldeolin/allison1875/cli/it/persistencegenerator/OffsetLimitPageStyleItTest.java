package com.spldeolin.allison1875.cli.it.persistencegenerator;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import org.junit.jupiter.api.Test;

/**
 * persistence-generator offset-limit-page-style 集成测试。
 *
 * @author Deolin 2026-05-14
 */
public class OffsetLimitPageStyleItTest extends PersistenceGeneratorItBaseTest {

    @Test
    void test() throws IOException {
        runPersistenceGenerator("offset-limit-page-style");

        File designDir = new File(basedir, "src/main/java/com/example/design");
        assertTrue(designDir.exists(), "design directory should exist");

        File designFile = new File(designDir, "TMessageDesign.java");
        assertTrue(designFile.exists(), "TMessageDesign.java should be generated");
        String designContent = new String(Files.readAllBytes(designFile.toPath()), StandardCharsets.UTF_8);

        assertTrue(designContent.contains("offset"), "Design page method should use 'offset' parameter");
        assertTrue(designContent.contains("limit"), "Design page method should use 'limit' parameter");

        assertFalse(designContent.contains("pageNo"), "Design should NOT use 'pageNo' when style is OFFSET_LIMIT");
        assertFalse(designContent.contains("pageSize"), "Design should NOT use 'pageSize' when style is OFFSET_LIMIT");

        File entityFile = new File(basedir, "src/main/java/com/example/entity/TMessageEntity.java");
        assertTrue(entityFile.exists(), "TMessageEntity.java should be generated");

        File mapperFile = new File(basedir, "src/main/java/com/example/mapper/TMessageMapper.java");
        assertTrue(mapperFile.exists(), "TMessageMapper.java should be generated");
    }

}

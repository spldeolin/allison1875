package com.spldeolin.allison1875.cli.it.persistencegenerator;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import org.junit.jupiter.api.Test;

/**
 * persistence-generator disable-design 集成测试。
 *
 * @author Deolin 2026-05-14
 */
public class DisableDesignItTest extends PersistenceGeneratorItBaseTest {

    @Test
    void test() throws IOException {
        runPersistenceGenerator("disable-design");

        assertTrue(new File(basedir, "src/main/java/com/example/entity/TSimpleEntity.java").exists(),
                "TSimpleEntity.java should still be generated");
        assertTrue(new File(basedir, "src/main/java/com/example/mapper/TSimpleMapper.java").exists(),
                "TSimpleMapper.java should still be generated");
        assertTrue(new File(basedir, "src/main/resources/mapper/TSimpleMapper.xml").exists(),
                "TSimpleMapper.xml should still be generated");

        File designDir = new File(basedir, "src/main/java/com/example/design");
        if (designDir.exists()) {
            File[] designFiles = designDir.listFiles((d, n) -> n.endsWith(".java"));
            assertTrue(designFiles == null || designFiles.length == 0,
                    "Design directory should be empty when enableGenerateDesign=false");
        }
    }

}

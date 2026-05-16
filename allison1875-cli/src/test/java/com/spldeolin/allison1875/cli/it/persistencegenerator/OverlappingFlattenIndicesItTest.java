package com.spldeolin.allison1875.cli.it.persistencegenerator;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import org.junit.jupiter.api.Test;

/**
 * persistence-generator overlapping-flatten-indices 集成测试。
 *
 * @author Deolin 2026-05-16
 */
public class OverlappingFlattenIndicesItTest extends PersistenceGeneratorItBaseTest {

    @Test
    void test() throws IOException {
        runPersistenceGenerator("overlapping-flatten-indices");

        File mapperFile = new File(basedir, "src/main/java/com/example/mapper/TOverlapIndexMapper.java");
        assertTrue(mapperFile.exists(), "TOverlapIndexMapper.java should be generated");
        String mapperContent = new String(Files.readAllBytes(mapperFile.toPath()), StandardCharsets.UTF_8);

        assertTrue(mapperContent.contains("TOverlapIndexEntity queryByA("),
                "queryByA should return single entity because column a is unique");
        assertTrue(mapperContent.contains("List<TOverlapIndexEntity> queryByAB("),
                "queryByAB should return List for non-unique composite index");
        assertFalse(mapperContent.contains("queryByAEx"), "Should not duplicate queryByA after flatten merge");
    }

}

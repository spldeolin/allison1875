package com.spldeolin.allison1875.cli.it.persistencegenerator;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import org.junit.jupiter.api.Test;

/**
 * persistence-generator no-modify-announce-off 集成测试。
 *
 * @author Deolin 2026-05-14
 */
public class NoModifyAnnounceOffItTest extends PersistenceGeneratorItBaseTest {

    private static final String NO_MODIFY_ANNOUNCE = "Any modifications may be overwritten by future code generations.";

    @Test
    void test() throws IOException {
        runPersistenceGenerator("no-modify-announce-off");

        // Entity 不包含 NO_MODIFY_ANNOUNCE
        File entityFile = new File(basedir, "src/main/java/com/example/entity/TTagEntity.java");
        assertTrue(entityFile.exists(), "TTagEntity.java should be generated");
        String entityContent = new String(Files.readAllBytes(entityFile.toPath()), StandardCharsets.UTF_8);
        assertFalse(entityContent.contains(NO_MODIFY_ANNOUNCE), "Entity should NOT contain NO_MODIFY_ANNOUNCE");

        // Mapper 不包含 NO_MODIFY_ANNOUNCE
        File mapperFile = new File(basedir, "src/main/java/com/example/mapper/TTagMapper.java");
        assertTrue(mapperFile.exists(), "TTagMapper.java should be generated");
        String mapperContent = new String(Files.readAllBytes(mapperFile.toPath()), StandardCharsets.UTF_8);
        assertFalse(mapperContent.contains(NO_MODIFY_ANNOUNCE), "Mapper should NOT contain NO_MODIFY_ANNOUNCE");

        // XML 不包含 NO_MODIFY_ANNOUNCE
        File xmlFile = new File(basedir, "src/main/resources/mapper/TTagMapper.xml");
        assertTrue(xmlFile.exists(), "TTagMapper.xml should be generated");
        String xmlContent = new String(Files.readAllBytes(xmlFile.toPath()), StandardCharsets.UTF_8);
        assertFalse(xmlContent.contains(NO_MODIFY_ANNOUNCE), "XML should NOT contain NO_MODIFY_ANNOUNCE");
    }

}

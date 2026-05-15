package com.spldeolin.allison1875.cli.it.persistencegenerator;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import org.junit.jupiter.api.Test;

/**
 * persistence-generator no-primary-key 集成测试。
 *
 * @author Deolin 2026-05-14
 */
public class NoPrimaryKeyItTest extends PersistenceGeneratorItBaseTest {

    @Test
    void test() throws IOException {
        runPersistenceGenerator("no-primary-key");

        File mapperFile = new File(basedir, "src/main/java/com/example/mapper/TLogMapper.java");
        assertTrue(mapperFile.exists(), "TLogMapper.java should be generated");
        String mapperContent = new String(Files.readAllBytes(mapperFile.toPath()), StandardCharsets.UTF_8);

        assertFalse(mapperContent.contains("queryById"), "Should NOT generate queryById for table without PK");
        assertFalse(mapperContent.contains("updateById"), "Should NOT generate updateById for table without PK");
        assertFalse(mapperContent.contains("deleteById"), "Should NOT generate deleteById for table without PK");
        assertFalse(mapperContent.contains("queryByIds"), "Should NOT generate queryByIds for table without PK");
        assertFalse(mapperContent.contains("queryByIdsEachId"),
                "Should NOT generate queryByIdsEachId for table without PK");

        assertTrue(mapperContent.contains("insert"), "Should still generate insert");
        assertTrue(mapperContent.contains("batchInsert"), "Should still generate batchInsert");
        assertTrue(mapperContent.contains("listAll"), "Should generate listAll for table without indexes");

        File xmlFile = new File(basedir, "src/main/resources/mapper/TLogMapper.xml");
        assertTrue(xmlFile.exists(), "TLogMapper.xml should be generated");
        String xmlContent = new String(Files.readAllBytes(xmlFile.toPath()), StandardCharsets.UTF_8);
        assertFalse(xmlContent.contains("queryById"), "XML should NOT contain queryById");
        assertFalse(xmlContent.contains("deleteById"), "XML should NOT contain deleteById");
        assertTrue(xmlContent.contains("insert"), "XML should contain insert");
        assertTrue(xmlContent.contains("listAll"), "XML should contain listAll");
        assertFalse(xmlContent.contains("<id column="), "ResultMap should NOT have <id> for table without PK");

        File entityFile = new File(basedir, "src/main/java/com/example/entity/TLogEntity.java");
        assertTrue(entityFile.exists(), "TLogEntity.java should be generated");
    }

}

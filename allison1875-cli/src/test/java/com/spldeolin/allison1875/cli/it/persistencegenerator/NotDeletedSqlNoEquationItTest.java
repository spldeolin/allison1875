package com.spldeolin.allison1875.cli.it.persistencegenerator;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import org.junit.jupiter.api.Test;

/**
 * persistence-generator not-deleted-sql-no-equation 集成测试。
 *
 * @author Deolin 2026-05-16
 */
public class NotDeletedSqlNoEquationItTest extends PersistenceGeneratorItBaseTest {

    @Test
    void test() throws IOException {
        runPersistenceGenerator("not-deleted-sql-no-equation");

        File xmlFile = new File(basedir, "src/main/resources/mapper/TArchiveMapper.xml");
        assertTrue(xmlFile.exists(), "TArchiveMapper.xml should be generated");
        String xmlContent = new String(Files.readAllBytes(xmlFile.toPath()), StandardCharsets.UTF_8);

        assertTrue(xmlContent.contains("<delete id=\"deleteById\""), "Should use hard delete when soft delete not recognized");
        assertFalse(xmlContent.contains("is_deleted IS NULL"), "notDeletedSql without '=' should be ignored");
        assertFalse(xmlContent.contains("is_deleted = NOW()"), "deletedSql should not apply without valid notDeletedSql");
    }

}

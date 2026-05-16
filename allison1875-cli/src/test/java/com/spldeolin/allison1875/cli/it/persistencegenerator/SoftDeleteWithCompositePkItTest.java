package com.spldeolin.allison1875.cli.it.persistencegenerator;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import org.junit.jupiter.api.Test;

/**
 * persistence-generator soft-delete-with-composite-pk 集成测试。
 *
 * @author Deolin 2026-05-16
 */
public class SoftDeleteWithCompositePkItTest extends PersistenceGeneratorItBaseTest {

    @Test
    void test() throws IOException {
        runPersistenceGenerator("soft-delete-with-composite-pk");

        File mapperFile = new File(basedir, "src/main/java/com/example/mapper/TUserRoleSdMapper.java");
        assertTrue(mapperFile.exists(), "TUserRoleSdMapper.java should be generated");
        String mapperContent = new String(Files.readAllBytes(mapperFile.toPath()), StandardCharsets.UTF_8);

        assertTrue(mapperContent.contains("@Param(\"userId\")"), "deleteById should use @Param for composite PK");
        assertTrue(mapperContent.contains("@Param(\"roleId\")"), "deleteById should use @Param for composite PK");
        assertFalse(mapperContent.contains("queryByIds"), "Composite PK should not generate queryByIds");
        assertFalse(mapperContent.contains("queryByIdsEachId"), "Composite PK should not generate queryByIdsEachId");

        File xmlFile = new File(basedir, "src/main/resources/mapper/TUserRoleSdMapper.xml");
        assertTrue(xmlFile.exists(), "TUserRoleSdMapper.xml should be generated");
        String xmlContent = new String(Files.readAllBytes(xmlFile.toPath()), StandardCharsets.UTF_8);

        assertTrue(xmlContent.contains("<update id=\"deleteById\""), "Soft delete should use update for deleteById");
        assertTrue(xmlContent.contains("SET is_deleted = 1"), "deleteById should set is_deleted = 1");
        assertTrue(xmlContent.contains("is_deleted = 0"), "Queries should filter not-deleted rows");
        assertTrue(xmlContent.contains("user_id = #{userId}"), "deleteById should match user_id");
        assertTrue(xmlContent.contains("role_id = #{roleId}"), "deleteById should match role_id");
    }

}

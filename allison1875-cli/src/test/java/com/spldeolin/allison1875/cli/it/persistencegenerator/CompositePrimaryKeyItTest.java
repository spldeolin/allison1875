package com.spldeolin.allison1875.cli.it.persistencegenerator;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import org.junit.jupiter.api.Test;

/**
 * persistence-generator composite-primary-key 集成测试。
 *
 * @author Deolin 2026-05-14
 */
public class CompositePrimaryKeyItTest extends PersistenceGeneratorItBaseTest {

    @Test
    void test() throws IOException {
        runPersistenceGenerator("composite-primary-key");

        File mapperFile = new File(basedir, "src/main/java/com/example/mapper/TUserRoleMapper.java");
        assertTrue(mapperFile.exists(), "TUserRoleMapper.java should be generated");
        String mapperContent = new String(Files.readAllBytes(mapperFile.toPath()), StandardCharsets.UTF_8);

        assertTrue(mapperContent.contains("queryById"), "Mapper should contain queryById");
        assertTrue(mapperContent.contains("@Param"), "queryById should use @Param for composite key");
        assertTrue(mapperContent.contains("userId"), "queryById should have userId param");
        assertTrue(mapperContent.contains("roleId"), "queryById should have roleId param");

        assertFalse(mapperContent.contains("queryByIds"), "Should NOT generate queryByIds for composite PK");
        assertFalse(mapperContent.contains("queryByIdsEachId"),
                "Should NOT generate queryByIdsEachId for composite PK");
        assertTrue(mapperContent.contains("deleteById"), "Mapper should contain deleteById");

        File xmlFile = new File(basedir, "src/main/resources/mapper/TUserRoleMapper.xml");
        assertTrue(xmlFile.exists(), "TUserRoleMapper.xml should be generated");
        String xmlContent = new String(Files.readAllBytes(xmlFile.toPath()), StandardCharsets.UTF_8);
        assertTrue(xmlContent.contains("user_id = #{userId}"), "XML queryById should match user_id");
        assertTrue(xmlContent.contains("role_id = #{roleId}"), "XML queryById should match role_id");

        File entityFile = new File(basedir, "src/main/java/com/example/entity/TUserRoleEntity.java");
        assertTrue(entityFile.exists(), "TUserRoleEntity.java should be generated");
        String entityContent = new String(Files.readAllBytes(entityFile.toPath()), StandardCharsets.UTF_8);
        assertTrue(entityContent.contains("userId"), "Entity should have userId");
        assertTrue(entityContent.contains("roleId"), "Entity should have roleId");
        assertTrue(entityContent.contains("grantedAt"), "Entity should have grantedAt");
    }

}

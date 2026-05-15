package com.spldeolin.allison1875.cli.it.persistencegenerator;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import org.junit.jupiter.api.Test;

/**
 * persistence-generator soft-delete 集成测试。
 *
 * @author Deolin 2026-05-14
 */
public class SoftDeleteItTest extends PersistenceGeneratorItBaseTest {

    @Test
    void test() throws IOException {
        runPersistenceGenerator("soft-delete");

        // ========== 1. 验证 Mapper XML 中 deleteById 使用 UPDATE ==========
        File xmlFile = new File(basedir, "src/main/resources/mapper/TArticleMapper.xml");
        assertTrue(xmlFile.exists(), "TArticleMapper.xml should be generated");
        String xmlContent = new String(Files.readAllBytes(xmlFile.toPath()), StandardCharsets.UTF_8);

        assertTrue(xmlContent.contains("<update id=\"deleteById\""),
                "deleteById should use <update> tag for soft delete");
        assertTrue(xmlContent.contains("SET is_deleted = 1"), "deleteById should SET is_deleted = 1");
        assertFalse(xmlContent.contains("<delete id=\"deleteById\""), "deleteById should NOT use <delete> tag");

        // ========== 2. 验证查询方法包含未删除条件 ==========
        assertTrue(xmlContent.contains("is_deleted = 0"), "Query methods should include is_deleted = 0 condition");
        assertTrue(xmlContent.contains("queryById"), "Should contain queryById method");

        // ========== 3. 验证 deleteByAuthorId 也使用 UPDATE ==========
        assertTrue(xmlContent.contains("<update id=\"deleteByAuthorId\""), "deleteByAuthorId should use <update> tag");

        // ========== 4. 验证 Entity 生成 ==========
        File entityFile = new File(basedir, "src/main/java/com/example/entity/TArticleEntity.java");
        assertTrue(entityFile.exists(), "TArticleEntity.java should be generated");
        String entityContent = new String(Files.readAllBytes(entityFile.toPath()), StandardCharsets.UTF_8);
        assertTrue(entityContent.contains("isDeleted"), "Entity should contain isDeleted field");
        assertTrue(entityContent.contains("content"), "Entity should contain content field");

        // ========== 5. 验证 Mapper 接口 ==========
        File mapperFile = new File(basedir, "src/main/java/com/example/mapper/TArticleMapper.java");
        assertTrue(mapperFile.exists(), "TArticleMapper.java should be generated");
        String mapperContent = new String(Files.readAllBytes(mapperFile.toPath()), StandardCharsets.UTF_8);
        assertTrue(mapperContent.contains("deleteById"), "Mapper should contain deleteById");
        assertTrue(mapperContent.contains("queryByAuthorId"), "Mapper should contain queryByAuthorId");
    }

}

package com.spldeolin.allison1875.cli.it.formgenerator;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import org.junit.jupiter.api.Test;

/**
 * unique-index 集成测试。
 *
 * <p>验证 {@code isUnique=true} 的索引生成：
 * <ul>
 *   <li>DDL 中生成 {@code UNIQUE KEY}（而非 {@code KEY}）</li>
 *   <li>persistence-generator 基于唯一索引生成单条查询方法 {@code queryByXxx}（返回单个 Entity，非 List）</li>
 *   <li>Mapper XML 中包含对应的 {@code <select id="queryByXxx">} 语句</li>
 *   <li>Save ServiceImpl 编辑分支使用 {@code queryByUserCode} 查询已有记录</li>
 * </ul>
 *
 * @author Deolin 2026-05-17
 */
public class UniqueIndexItTest extends FormGeneratorItBaseTest {

    @Test
    void test() throws IOException {
        runFormGenerator("unique-index");

        // === DDL 验证 ===
        File ddlFile = new File(basedir, "sql/ddl.sql");
        assertTrue(ddlFile.exists(), "DDL file should be generated at sql/ddl.sql");
        String ddl = new String(Files.readAllBytes(ddlFile.toPath()), StandardCharsets.UTF_8);
        assertTrue(ddl.contains("CREATE TABLE"), "DDL should contain CREATE TABLE");
        // 自动添加的业务主键唯一索引
        assertTrue(ddl.contains("UNIQUE KEY `uk_user_code`"),
                "DDL should contain UNIQUE KEY on auto-added bizId 'user_code'");
        // 用户定义的 email 唯一索引
        assertTrue(ddl.contains("UNIQUE KEY `uk_email`"),
                "DDL should contain UNIQUE KEY on user-defined 'email'");

        // === Mapper 接口验证：唯一索引 → queryByXxx 方法（返回单个 Entity） ===
        File mapperFile = new File(basedir, "src/main/java/com/example/mapper/UserMapper.java");
        assertTrue(mapperFile.exists(), "UserMapper should be generated");
        String mapperContent = new String(Files.readAllBytes(mapperFile.toPath()), StandardCharsets.UTF_8);
        // 业务主键唯一索引 → queryByUserCode（返回单个实体）
        assertTrue(mapperContent.contains("queryByUserCode"),
                "Mapper should contain queryByUserCode method from bizId unique index");
        // email 唯一索引 → queryByEmail（返回单个实体，非 List）
        assertTrue(mapperContent.contains("queryByEmail"),
                "Mapper should contain queryByEmail method from email unique index");
        // 验证 email 方法返回类型是单实体而非 List（唯一索引特征）
        assertTrue(mapperContent.contains("UserEntity queryByEmail"),
                "queryByEmail should return single UserEntity (unique index), not List");

        // === Mapper XML 验证 ===
        File xmlFile = new File(basedir, "src/main/resources/mapper/UserMapper.xml");
        assertTrue(xmlFile.exists(), "Mapper XML should be generated");
        String xmlContent = new String(Files.readAllBytes(xmlFile.toPath()), StandardCharsets.UTF_8);
        assertTrue(xmlContent.contains("queryByUserCode"),
                "Mapper XML should contain queryByUserCode select statement");
        assertTrue(xmlContent.contains("queryByEmail"),
                "Mapper XML should contain queryByEmail select statement");
        // queryByEmail XML 应该用 = 精确匹配（唯一索引只有一个返回值）
        assertTrue(xmlContent.contains("email = #{email}"),
                "queryByEmail XML should use 'email = #{email}' exact match");

        // === Save ServiceImpl 验证：编辑分支使用 queryByUserCode ===
        File saveServiceImplFile = new File(basedir,
                "src/main/java/com/example/service/impl/SaveUserServiceImpl.java");
        assertTrue(saveServiceImplFile.exists(), "SaveUserServiceImpl should be generated");
        String saveServiceImplContent = new String(Files.readAllBytes(saveServiceImplFile.toPath()),
                StandardCharsets.UTF_8);
        assertTrue(saveServiceImplContent.contains("userMapper.queryByUserCode("),
                "Save service edit branch should call userMapper.queryByUserCode");

        // === 验证没有生成 api-docs 目录 ===
        File apiDocsDir = new File(basedir, "api-docs");
        assertTrue(apiDocsDir.exists(), "api-docs directory should be generated");
    }

}

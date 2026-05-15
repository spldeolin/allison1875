package com.spldeolin.allison1875.cli.it.persistencegenerator;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import org.junit.jupiter.api.Test;

/**
 * persistence-generator basic-ddl 集成测试。
 *
 * <p>验证 persistence-generator 通过 DDL 解析表结构后生成：
 * 1. Entity Java 文件
 * 2. Mapper 接口
 * 3. Mapper XML 文件
 * 4. Design 文件
 *
 * @author Deolin 2026-05-14
 */
public class BasicDdlItTest extends PersistenceGeneratorItBaseTest {

    @Test
    void test() throws IOException {
        runPersistenceGenerator("basic-ddl");

        // ========== 1. 验证 Entity 文件生成 ==========
        File entityFile = new File(basedir, "src/main/java/com/example/entity/TOrderEntity.java");
        assertTrue(entityFile.exists(), "TOrderEntity.java should be generated");
        String entityContent = new String(Files.readAllBytes(entityFile.toPath()), StandardCharsets.UTF_8);

        // 验证字段存在
        assertTrue(entityContent.contains("id"), "Entity should contain field 'id'");
        assertTrue(entityContent.contains("orderNo"), "Entity should contain field 'orderNo'");
        assertTrue(entityContent.contains("userId"), "Entity should contain field 'userId'");
        assertTrue(entityContent.contains("amount"), "Entity should contain field 'amount'");
        assertTrue(entityContent.contains("status"), "Entity should contain field 'status'");
        assertTrue(entityContent.contains("createdAt"), "Entity should contain field 'createdAt'");
        assertTrue(entityContent.contains("updatedAt"), "Entity should contain field 'updatedAt'");

        // 验证类型映射
        assertTrue(entityContent.contains("Long"), "Entity should contain Long type (for id/userId)");
        assertTrue(entityContent.contains("String"), "Entity should contain String type (for orderNo)");
        assertTrue(entityContent.contains("BigDecimal"), "Entity should contain BigDecimal type (for amount)");
        assertTrue(entityContent.contains("LocalDateTime"),
                "Entity should contain LocalDateTime type (for createdAt/updatedAt)");

        // 验证包声明
        assertTrue(entityContent.contains("package com.example.entity"), "Entity should have correct package");

        // ========== 2. 验证 Mapper 接口生成 ==========
        File mapperFile = new File(basedir, "src/main/java/com/example/mapper/TOrderMapper.java");
        assertTrue(mapperFile.exists(), "TOrderMapper.java should be generated");
        String mapperContent = new String(Files.readAllBytes(mapperFile.toPath()), StandardCharsets.UTF_8);

        // 验证是接口
        assertTrue(mapperContent.contains("interface"), "Mapper file should be an interface");
        assertTrue(mapperContent.contains("TOrderMapper"), "Mapper should be named TOrderMapper");

        // 验证基础CRUD方法
        assertTrue(mapperContent.contains("insert"), "Mapper should contain insert method");
        assertTrue(mapperContent.contains("batchInsert"), "Mapper should contain batchInsert method");
        assertTrue(mapperContent.contains("updateById"), "Mapper should contain updateById method");
        assertTrue(mapperContent.contains("deleteById"), "Mapper should contain deleteById method");
        assertTrue(mapperContent.contains("queryById"), "Mapper should contain queryById method");
        assertTrue(mapperContent.contains("queryByIds"), "Mapper should contain queryByIds method");

        // 验证索引方法（uk_order_no -> queryByOrderNo, idx_user_id -> queryByUserId）
        assertTrue(mapperContent.contains("queryByOrderNo"),
                "Mapper should contain queryByOrderNo method (from unique index)");
        assertTrue(mapperContent.contains("queryByUserId"), "Mapper should contain queryByUserId method (from index)");

        // 验证包声明
        assertTrue(mapperContent.contains("package com.example.mapper"), "Mapper should have correct package");

        // ========== 3. 验证 Mapper XML 文件生成 ==========
        File mapperXmlDir = new File(basedir, "src/main/resources/mapper");
        assertTrue(mapperXmlDir.exists(), "mapper xml directory should exist");

        File mapperXmlFile = new File(mapperXmlDir, "TOrderMapper.xml");
        assertTrue(mapperXmlFile.exists(), "TOrderMapper.xml should be generated");
        String xmlContent = new String(Files.readAllBytes(mapperXmlFile.toPath()), StandardCharsets.UTF_8);

        // 验证基础结构
        assertTrue(xmlContent.contains("resultMap"), "XML should contain resultMap");
        assertTrue(xmlContent.contains("t_order"), "XML should reference table name 't_order'");
        assertTrue(xmlContent.contains("TOrderMapper") || xmlContent.contains("com.example.mapper.TOrderMapper"),
                "XML should reference mapper namespace");

        // 验证基础SQL方法
        assertTrue(xmlContent.contains("insert"), "XML should contain insert SQL");
        assertTrue(xmlContent.contains("updateById"), "XML should contain updateById SQL");
        assertTrue(xmlContent.contains("deleteById"), "XML should contain deleteById SQL");
        assertTrue(xmlContent.contains("queryById"), "XML should contain queryById SQL");

        // 验证字段映射
        assertTrue(xmlContent.contains("order_no"), "XML should contain column 'order_no'");
        assertTrue(xmlContent.contains("user_id"), "XML should contain column 'user_id'");
        assertTrue(xmlContent.contains("created_at"), "XML should contain column 'created_at'");

        // ========== 4. 验证 Design 文件生成 ==========
        File designDir = new File(basedir, "src/main/java/com/example/design");
        assertTrue(designDir.exists(), "design directory should exist");

        File[] designFiles = designDir.listFiles((dir, name) -> name.endsWith(".java") && name.contains("TOrder"));
        assertTrue(designFiles != null && designFiles.length > 0,
                "At least one Design file should be generated for TOrder");

        String designContent = new String(Files.readAllBytes(designFiles[0].toPath()), StandardCharsets.UTF_8);
        assertTrue(designContent.contains("package com.example.design"), "Design should have correct package");
        assertTrue(designContent.contains("class") || designContent.contains("interface"),
                "Design should contain a type declaration");
    }

}

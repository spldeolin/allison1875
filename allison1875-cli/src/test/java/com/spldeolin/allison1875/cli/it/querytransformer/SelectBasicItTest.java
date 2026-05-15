package com.spldeolin.allison1875.cli.it.querytransformer;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import org.junit.jupiter.api.Test;

/**
 * query-transformer select-basic 集成测试。
 *
 * <p>验证基本 SELECT 查询功能：
 * 1. Mapper 接口中生成 findById / listAll / countByUserId 方法
 * 2. Mapper XML 中生成对应 SQL（LIMIT 1、COUNT(*)、user_id 条件）
 * 3. Service 文件中 Design 链被替换为 Mapper 调用
 *
 * @author Deolin 2026-05-14
 */
public class SelectBasicItTest extends QueryTransformerItBaseTest {

    @Test
    void test() throws IOException {
        runQueryTransformer("select-basic");

        // ========== 1. Mapper 接口验证 ==========
        File mapperFile = new File(basedir, "src/main/java/com/example/mapper/TOrderMapper.java");
        assertTrue(mapperFile.exists(), "TOrderMapper.java should exist");
        String mapperContent = new String(Files.readAllBytes(mapperFile.toPath()), StandardCharsets.UTF_8);

        assertTrue(mapperContent.contains("findById"), "Mapper should contain findById method");
        assertTrue(mapperContent.contains("listAll"), "Mapper should contain listAll method");
        assertTrue(mapperContent.contains("countByUserId"), "Mapper should contain countByUserId method");

        // ========== 2. Mapper XML 验证 ==========
        File mapperXmlFile = new File(basedir, "src/main/resources/mapper/TOrderMapper.xml");
        assertTrue(mapperXmlFile.exists(), "TOrderMapper.xml should exist");
        String xmlContent = new String(Files.readAllBytes(mapperXmlFile.toPath()), StandardCharsets.UTF_8);

        // findById: SELECT with LIMIT 1
        assertTrue(xmlContent.contains("id='findById'") || xmlContent.contains("id=\"findById\""),
                "XML should contain findById select");
        assertTrue(xmlContent.contains("LIMIT 1"), "one() should generate LIMIT 1");

        // listAll: SELECT without WHERE
        assertTrue(xmlContent.contains("id='listAll'") || xmlContent.contains("id=\"listAll\""),
                "XML should contain listAll select");

        // countByUserId: SELECT COUNT(*)
        assertTrue(xmlContent.contains("id='countByUserId'") || xmlContent.contains("id=\"countByUserId\""),
                "XML should contain countByUserId select");
        assertTrue(xmlContent.contains("COUNT(*)"), "count() should generate COUNT(*)");
        assertTrue(xmlContent.contains("user_id"), "XML should contain user_id column");

        // ========== 3. Service 文件验证 ==========
        File serviceFile = new File(basedir, "src/main/java/com/example/service/OrderService.java");
        assertTrue(serviceFile.exists(), "OrderService.java should exist");
        String serviceContent = new String(Files.readAllBytes(serviceFile.toPath()), StandardCharsets.UTF_8);

        // Design 链已被替换
        assertFalse(serviceContent.contains("TOrderDesign."), "Design chain should be replaced");

        // Mapper 被注入
        assertTrue(serviceContent.contains("TOrderMapper"), "Mapper should be injected");
        assertTrue(serviceContent.contains("tOrderMapper"), "Mapper field should exist");

        // 方法调用
        assertTrue(serviceContent.contains("tOrderMapper.findById"), "Should call mapper.findById");
        assertTrue(serviceContent.contains("tOrderMapper.listAll"), "Should call mapper.listAll");
        assertTrue(serviceContent.contains("tOrderMapper.countByUserId"), "Should call mapper.countByUserId");
    }

}

package com.spldeolin.allison1875.cli.it.querytransformer;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import org.junit.jupiter.api.Test;

/**
 * query-transformer select-map-group 集成测试。
 *
 * <p>验证 map()/group() 聚合方法功能：
 * 1. Mapper 接口中包含 @MapKey 注解
 * 2. Mapper XML 中生成对应 SQL
 * 3. Service 文件中 Group 方法生成 Collectors.groupingBy 调用
 *
 * @author Deolin 2026-05-15
 */
public class SelectMapGroupItTest extends QueryTransformerItBaseTest {

    @Test
    void test() throws IOException {
        runQueryTransformer("select-map-group");

        // ========== 1. Mapper 接口验证 ==========
        File mapperFile = new File(basedir, "src/main/java/com/example/mapper/TOrderMapper.java");
        assertTrue(mapperFile.exists(), "TOrderMapper.java should exist");
        String mapperContent = new String(Files.readAllBytes(mapperFile.toPath()), StandardCharsets.UTF_8);

        assertTrue(mapperContent.contains("mapByUserId"), "Mapper should contain mapByUserId method");
        assertTrue(mapperContent.contains("groupByStatus"), "Mapper should contain groupByStatus method");
        assertTrue(mapperContent.contains("mapByUserIdWithCondition"),
                "Mapper should contain mapByUserIdWithCondition method");

        // MAP 返回风格应有 @MapKey 注解
        assertTrue(mapperContent.contains("@MapKey"), "MAP return style should generate @MapKey annotation");
        assertTrue(mapperContent.contains("\"userId\""), "@MapKey should reference userId");

        // ========== 2. Mapper XML 验证 ==========
        File mapperXmlFile = new File(basedir, "src/main/resources/mapper/TOrderMapper.xml");
        assertTrue(mapperXmlFile.exists(), "TOrderMapper.xml should exist");
        String xmlContent = new String(Files.readAllBytes(mapperXmlFile.toPath()), StandardCharsets.UTF_8);

        // mapByUserId: 无 WHERE
        assertTrue(xmlContent.contains("id='mapByUserId'") || xmlContent.contains("id=\"mapByUserId\""),
                "XML should contain mapByUserId select");

        // groupByStatus: 无 WHERE
        assertTrue(xmlContent.contains("id='groupByStatus'") || xmlContent.contains("id=\"groupByStatus\""),
                "XML should contain groupByStatus select");

        // mapByUserIdWithCondition: 有 WHERE status =
        assertTrue(xmlContent.contains("id='mapByUserIdWithCondition'") || xmlContent.contains(
                "id=\"mapByUserIdWithCondition\""), "XML should contain mapByUserIdWithCondition select");
        assertTrue(xmlContent.contains("status"), "XML should contain status column for condition");

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
        assertTrue(serviceContent.contains("tOrderMapper.mapByUserId"), "Should call mapper.mapByUserId");
        assertTrue(serviceContent.contains("tOrderMapper.groupByStatus"), "Should call mapper.groupByStatus");
        assertTrue(serviceContent.contains("tOrderMapper.mapByUserIdWithCondition"),
                "Should call mapper.mapByUserIdWithCondition");

        // GROUP 返回风格应有 Collectors.groupingBy 调用
        assertTrue(serviceContent.contains("groupingBy"),
                "GROUP return style should generate Collectors.groupingBy call");
    }

}
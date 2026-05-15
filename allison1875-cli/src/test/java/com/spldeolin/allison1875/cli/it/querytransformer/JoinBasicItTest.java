package com.spldeolin.allison1875.cli.it.querytransformer;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import org.junit.jupiter.api.Test;

/**
 * query-transformer join-basic 集成测试。
 *
 * <p>验证基本 JOIN 查询功能：
 * 1. Mapper XML 中包含 LEFT JOIN 和 t1/t2 别名
 * 2. Record DTO 生成包含 joined 表字段
 * 3. Service 中 Design 链被替换
 *
 * @author Deolin 2026-05-15
 */
public class JoinBasicItTest extends QueryTransformerItBaseTest {

    @Test
    void test() throws IOException {
        runQueryTransformer("join-basic");

        // ========== 1. Mapper 接口验证 ==========
        File mapperFile = new File(basedir, "src/main/java/com/example/mapper/TOrderMapper.java");
        assertTrue(mapperFile.exists(), "TOrderMapper.java should exist");
        String mapperContent = new String(Files.readAllBytes(mapperFile.toPath()), StandardCharsets.UTF_8);

        assertTrue(mapperContent.contains("listOrderWithUserName"),
                "Mapper should contain listOrderWithUserName method");

        // ========== 2. Mapper XML 验证 ==========
        File mapperXmlFile = new File(basedir, "src/main/resources/mapper/TOrderMapper.xml");
        assertTrue(mapperXmlFile.exists(), "TOrderMapper.xml should exist");
        String xmlContent = new String(Files.readAllBytes(mapperXmlFile.toPath()), StandardCharsets.UTF_8);

        // listOrderWithUserName: LEFT JOIN
        assertTrue(xmlContent.contains("id='listOrderWithUserName'") || xmlContent.contains(
                "id=\"listOrderWithUserName\""), "XML should contain listOrderWithUserName select");
        assertTrue(xmlContent.contains("LEFT JOIN"), "XML should contain LEFT JOIN clause");
        assertTrue(xmlContent.contains("t_user"), "XML should contain t_user table name in JOIN");

        // t1/t2 别名
        assertTrue(xmlContent.contains("t1."), "XML should use t1 alias for main table");
        assertTrue(xmlContent.contains("t2."), "XML should use t2 alias for joined table");

        // ========== 3. Record DTO 验证 ==========
        File recordDir = new File(basedir, "src/main/java/com/example/dto/record");
        assertTrue(recordDir.exists(), "Record DTO directory should exist");
        File[] recordFiles = recordDir.listFiles();
        assertTrue(recordFiles != null && recordFiles.length > 0,
                "At least one Record DTO should be generated for join query");

        StringBuilder recordContent = new StringBuilder();
        for (File f : recordFiles) {
            recordContent.append(new String(Files.readAllBytes(f.toPath()), StandardCharsets.UTF_8));
        }
        String recordText = recordContent.toString();
        assertTrue(recordText.contains("userName") || recordText.contains("UserName"),
                "Record DTO should contain joined userName field");

        // ========== 4. Service 文件验证 ==========
        File serviceFile = new File(basedir, "src/main/java/com/example/service/OrderService.java");
        assertTrue(serviceFile.exists(), "OrderService.java should exist");
        String serviceContent = new String(Files.readAllBytes(serviceFile.toPath()), StandardCharsets.UTF_8);

        // Design 链已被替换
        assertFalse(serviceContent.contains("TOrderDesign.select"), "Design chain should be replaced");

        // Mapper 被注入
        assertTrue(serviceContent.contains("TOrderMapper"), "Mapper should be injected");
        assertTrue(serviceContent.contains("tOrderMapper"), "Mapper field should exist");
    }

}
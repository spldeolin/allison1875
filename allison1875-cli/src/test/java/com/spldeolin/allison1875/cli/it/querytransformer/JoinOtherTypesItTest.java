package com.spldeolin.allison1875.cli.it.querytransformer;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import org.junit.jupiter.api.Test;

/**
 * query-transformer join-other-types 集成测试。
 *
 * <p>验证 JOIN 查询支持 LEFT JOIN 以外的其他 JOIN 类型：
 * 1. rightJoin() → XML 包含 RIGHT JOIN
 * 2. innerJoin() → XML 包含 INNER JOIN
 * 3. outerJoin() → XML 包含 OUTER JOIN
 *
 * @author Deolin 2026-05-17
 */
public class JoinOtherTypesItTest extends QueryTransformerItBaseTest {

    @Test
    void test() throws IOException {
        runQueryTransformer("join-other-types");

        // ========== 1. Mapper 接口验证 ==========
        File mapperFile = new File(basedir, "src/main/java/com/example/mapper/TOrderMapper.java");
        assertTrue(mapperFile.exists(), "TOrderMapper.java should exist");
        String mapperContent = new String(Files.readAllBytes(mapperFile.toPath()), StandardCharsets.UTF_8);

        assertTrue(mapperContent.contains("listByRightJoin"), "Mapper should contain listByRightJoin method");
        assertTrue(mapperContent.contains("listByInnerJoin"), "Mapper should contain listByInnerJoin method");
        assertTrue(mapperContent.contains("listByOuterJoin"), "Mapper should contain listByOuterJoin method");

        // ========== 2. Mapper XML 验证 ==========
        File mapperXmlFile = new File(basedir, "src/main/resources/mapper/TOrderMapper.xml");
        assertTrue(mapperXmlFile.exists(), "TOrderMapper.xml should exist");
        String xmlContent = new String(Files.readAllBytes(mapperXmlFile.toPath()), StandardCharsets.UTF_8);

        // listByRightJoin: RIGHT JOIN
        assertTrue(xmlContent.contains("id='listByRightJoin'") || xmlContent.contains(
                "id=\"listByRightJoin\""), "XML should contain listByRightJoin select");
        assertTrue(xmlContent.contains("RIGHT JOIN"), "XML should contain RIGHT JOIN for rightJoin()");

        // listByInnerJoin: INNER JOIN
        assertTrue(xmlContent.contains("id='listByInnerJoin'") || xmlContent.contains(
                "id=\"listByInnerJoin\""), "XML should contain listByInnerJoin select");
        assertTrue(xmlContent.contains("INNER JOIN"), "XML should contain INNER JOIN for innerJoin()");

        // listByOuterJoin: OUTER JOIN
        assertTrue(xmlContent.contains("id='listByOuterJoin'") || xmlContent.contains(
                "id=\"listByOuterJoin\""), "XML should contain listByOuterJoin select");
        assertTrue(xmlContent.contains("OUTER JOIN"), "XML should contain OUTER JOIN for outerJoin()");

        // t1/t2 别名
        assertTrue(xmlContent.contains("t1.") || xmlContent.contains("t1 "),
                "XML should use t1 alias for main table");
        assertTrue(xmlContent.contains("t2.") || xmlContent.contains("t2 "),
                "XML should use t2 alias for joined table");

        // ========== 3. Record DTO 验证 ==========
        File recordDir = new File(basedir, "src/main/java/com/example/dto/record");
        assertTrue(recordDir.exists(), "Record DTO directory should exist");
        File[] recordFiles = recordDir.listFiles();
        assertTrue(recordFiles != null && recordFiles.length > 0,
                "At least one Record DTO should be generated for join queries");
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
        assertTrue(serviceContent.contains("TOrderMapper"), "Mapper should be injected");
        assertTrue(serviceContent.contains("tOrderMapper"), "Mapper field should exist");
    }

}
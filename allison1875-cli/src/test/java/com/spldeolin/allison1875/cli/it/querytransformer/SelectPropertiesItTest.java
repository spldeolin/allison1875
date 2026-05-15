package com.spldeolin.allison1875.cli.it.querytransformer;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import org.junit.jupiter.api.Test;

/**
 * query-transformer select-properties 集成测试。
 *
 * <p>验证 select 指定属性功能：
 * 1. 单属性场景：查询结果类型为 List&lt;String&gt;
 * 2. 多属性场景：生成 Record DTO
 * 3. 多属性+where 条件场景
 *
 * @author Deolin 2026-05-14
 */
public class SelectPropertiesItTest extends QueryTransformerItBaseTest {

    @Test
    void test() throws IOException {
        runQueryTransformer("select-properties");

        // ========== 1. Mapper 接口验证 ==========
        File mapperFile = new File(basedir, "src/main/java/com/example/mapper/TOrderMapper.java");
        assertTrue(mapperFile.exists(), "TOrderMapper.java should exist");
        String mapperContent = new String(Files.readAllBytes(mapperFile.toPath()), StandardCharsets.UTF_8);

        assertTrue(mapperContent.contains("listOrderNos"), "Mapper should contain listOrderNos method");
        assertTrue(mapperContent.contains("listOrderSummaries"), "Mapper should contain listOrderSummaries method");
        assertTrue(mapperContent.contains("listOrderSummariesByStatus"),
                "Mapper should contain listOrderSummariesByStatus method");

        // ========== 2. Mapper XML 验证 ==========
        File mapperXmlFile = new File(basedir, "src/main/resources/mapper/TOrderMapper.xml");
        assertTrue(mapperXmlFile.exists(), "TOrderMapper.xml should exist");
        String xmlContent = new String(Files.readAllBytes(mapperXmlFile.toPath()), StandardCharsets.UTF_8);

        assertTrue(xmlContent.contains("id='listOrderNos'") || xmlContent.contains("id=\"listOrderNos\""),
                "XML should contain listOrderNos select");
        assertTrue(xmlContent.contains("order_no"), "XML should contain order_no column");

        assertTrue(xmlContent.contains("id='listOrderSummaries'") || xmlContent.contains("id=\"listOrderSummaries\""),
                "XML should contain listOrderSummaries select");
        assertTrue(xmlContent.contains("user_id"), "XML should contain user_id column");

        assertTrue(xmlContent.contains("id='listOrderSummariesByStatus'") || xmlContent.contains(
                "id=\"listOrderSummariesByStatus\""), "XML should contain listOrderSummariesByStatus select");

        // ========== 3. Record DTO 验证 ==========
        File recordDir = new File(basedir, "src/main/java/com/example/dto/record");
        assertTrue(recordDir.exists(), "Record DTO directory should exist");

        File[] recordFiles = recordDir.listFiles();
        assertTrue(recordFiles != null && recordFiles.length > 0, "At least one Record DTO should be generated");

        StringBuilder recordContent = new StringBuilder();
        for (File f : recordFiles) {
            recordContent.append(new String(Files.readAllBytes(f.toPath()), StandardCharsets.UTF_8));
        }
        String recordText = recordContent.toString();
        assertTrue(recordText.contains("orderNo"), "Record DTO should contain orderNo field");
        assertTrue(recordText.contains("userId"), "Record DTO should contain userId field");
        assertTrue(recordText.contains("amount"), "Record DTO should contain amount field");

        // ========== 4. Service 文件验证 ==========
        File serviceFile = new File(basedir, "src/main/java/com/example/service/OrderService.java");
        assertTrue(serviceFile.exists(), "OrderService.java should exist");
        String serviceContent = new String(Files.readAllBytes(serviceFile.toPath()), StandardCharsets.UTF_8);

        assertFalse(serviceContent.contains("TOrderDesign."), "Design chain should be replaced");
        assertTrue(serviceContent.contains("TOrderMapper"), "Mapper should be injected");
        assertTrue(serviceContent.contains("tOrderMapper"), "Mapper field should exist");

        assertTrue(serviceContent.contains("tOrderMapper.listOrderNos"), "Should call mapper.listOrderNos");
        assertTrue(serviceContent.contains("tOrderMapper.listOrderSummaries"), "Should call mapper.listOrderSummaries");
        assertTrue(serviceContent.contains("tOrderMapper.listOrderSummariesByStatus"),
                "Should call mapper.listOrderSummariesByStatus");
    }

}

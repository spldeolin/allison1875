package com.spldeolin.allison1875.cli.it.querytransformer;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import org.junit.jupiter.api.Test;

/**
 * query-transformer order-by 集成测试。
 *
 * <p>验证 ORDER BY 排序功能：
 * 1. 单字段 ASC / DESC
 * 2. 多字段排序
 * 3. WHERE + ORDER BY 组合
 *
 * @author Deolin 2026-05-15
 */
public class OrderByItTest extends QueryTransformerItBaseTest {

    @Test
    void test() throws IOException {
        runQueryTransformer("order-by");

        // ========== 1. Mapper XML 验证 ==========
        File mapperXmlFile = new File(basedir, "src/main/resources/mapper/TOrderMapper.xml");
        assertTrue(mapperXmlFile.exists(), "TOrderMapper.xml should exist");
        String xmlContent = new String(Files.readAllBytes(mapperXmlFile.toPath()), StandardCharsets.UTF_8);

        // 单字段 ASC
        assertTrue(xmlContent.contains("id='listOrderByCreatedAtAsc'") || xmlContent.contains(
                "id=\"listOrderByCreatedAtAsc\""), "XML should contain listOrderByCreatedAtAsc");
        assertTrue(xmlContent.contains("ORDER BY"), "XML should contain ORDER BY clause");
        assertTrue(xmlContent.contains("created_at"), "ORDER BY should reference created_at column");

        // 单字段 DESC
        assertTrue(xmlContent.contains("id='listOrderByAmountDesc'") || xmlContent.contains(
                "id=\"listOrderByAmountDesc\""), "XML should contain listOrderByAmountDesc");
        assertTrue(xmlContent.contains("amount DESC"), "ORDER BY should contain 'amount DESC'");

        // 多字段排序
        assertTrue(xmlContent.contains("id='listOrderByMultiFields'") || xmlContent.contains(
                "id=\"listOrderByMultiFields\""), "XML should contain listOrderByMultiFields");

        // WHERE + ORDER BY 组合
        assertTrue(xmlContent.contains("id='listByUserIdOrderByCreatedAt'") || xmlContent.contains(
                "id=\"listByUserIdOrderByCreatedAt\""), "XML should contain listByUserIdOrderByCreatedAt");
        assertTrue(xmlContent.contains("user_id"), "Should have WHERE user_id condition");

        // ========== 2. Mapper 接口验证 ==========
        File mapperFile = new File(basedir, "src/main/java/com/example/mapper/TOrderMapper.java");
        assertTrue(mapperFile.exists(), "TOrderMapper.java should exist");
        String mapperContent = new String(Files.readAllBytes(mapperFile.toPath()), StandardCharsets.UTF_8);

        assertTrue(mapperContent.contains("listOrderByCreatedAtAsc"), "Mapper should contain listOrderByCreatedAtAsc");
        assertTrue(mapperContent.contains("listOrderByAmountDesc"), "Mapper should contain listOrderByAmountDesc");
        assertTrue(mapperContent.contains("listOrderByMultiFields"), "Mapper should contain listOrderByMultiFields");
        assertTrue(mapperContent.contains("listByUserIdOrderByCreatedAt"),
                "Mapper should contain listByUserIdOrderByCreatedAt");

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
        assertTrue(serviceContent.contains("tOrderMapper.listOrderByCreatedAtAsc"),
                "Should call mapper.listOrderByCreatedAtAsc");
        assertTrue(serviceContent.contains("tOrderMapper.listOrderByAmountDesc"),
                "Should call mapper.listOrderByAmountDesc");
        assertTrue(serviceContent.contains("tOrderMapper.listOrderByMultiFields"),
                "Should call mapper.listOrderByMultiFields");
        assertTrue(serviceContent.contains("tOrderMapper.listByUserIdOrderByCreatedAt"),
                "Should call mapper.listByUserIdOrderByCreatedAt");
    }

}
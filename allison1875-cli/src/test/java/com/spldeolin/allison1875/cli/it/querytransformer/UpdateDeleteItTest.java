package com.spldeolin.allison1875.cli.it.querytransformer;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import org.junit.jupiter.api.Test;

/**
 * query-transformer update-delete 集成测试。
 *
 * <p>验证 UPDATE 和 DELETE DSL 链：
 * 1. UPDATE 单字段/多字段 + WHERE 条件
 * 2. DELETE 按 ID / 按条件
 * 3. Mapper 接口方法返回 int
 *
 * @author Deolin 2026-05-15
 */
public class UpdateDeleteItTest extends QueryTransformerItBaseTest {

    @Test
    void test() throws IOException {
        runQueryTransformer("update-delete");

        // ========== 1. Mapper XML 验证 ==========
        File mapperXmlFile = new File(basedir, "src/main/resources/mapper/TOrderMapper.xml");
        assertTrue(mapperXmlFile.exists(), "TOrderMapper.xml should exist");
        String xmlContent = new String(Files.readAllBytes(mapperXmlFile.toPath()), StandardCharsets.UTF_8);

        // UPDATE: updateStatusById
        assertTrue(xmlContent.contains("id='updateStatusById'") || xmlContent.contains("id=\"updateStatusById\""),
                "XML should contain updateStatusById");
        assertTrue(xmlContent.contains("<update") || xmlContent.contains("update"), "XML should contain update tag");
        assertTrue(xmlContent.contains("UPDATE t_order"), "UPDATE should reference t_order");
        assertTrue(xmlContent.contains("status = #{status}"), "SET should contain status = #{status}");

        // UPDATE: updateAmountAndRemarkById (多字段)
        assertTrue(xmlContent.contains("id='updateAmountAndRemarkById'") || xmlContent.contains(
                "id=\"updateAmountAndRemarkById\""), "XML should contain updateAmountAndRemarkById");
        assertTrue(xmlContent.contains("amount") || xmlContent.contains("amount ="), "SET should contain amount");
        assertTrue(xmlContent.contains("remark"), "SET should contain remark");

        // DELETE: deleteOrderById
        assertTrue(xmlContent.contains("id='deleteOrderById'") || xmlContent.contains("id=\"deleteOrderById\""),
                "XML should contain deleteOrderById");
        assertTrue(xmlContent.contains("<delete") || xmlContent.contains("delete"), "XML should contain delete tag");
        assertTrue(xmlContent.contains("DELETE FROM t_order"), "DELETE should reference t_order");

        // DELETE: deleteByUserId
        assertTrue(xmlContent.contains("id='deleteByUserId'") || xmlContent.contains("id=\"deleteByUserId\""),
                "XML should contain deleteByUserId");

        // ========== 2. Mapper 接口验证 ==========
        File mapperFile = new File(basedir, "src/main/java/com/example/mapper/TOrderMapper.java");
        assertTrue(mapperFile.exists(), "TOrderMapper.java should exist");
        String mapperContent = new String(Files.readAllBytes(mapperFile.toPath()), StandardCharsets.UTF_8);

        assertTrue(mapperContent.contains("updateStatusById"), "Mapper should contain updateStatusById method");
        assertTrue(mapperContent.contains("updateAmountAndRemarkById"),
                "Mapper should contain updateAmountAndRemarkById method");
        assertTrue(mapperContent.contains("deleteOrderById"), "Mapper should contain deleteOrderById method");
        assertTrue(mapperContent.contains("deleteByUserId"), "Mapper should contain deleteByUserId method");

        // UPDATE/DELETE 方法返回 int
        assertTrue(mapperContent.contains("int updateStatusById") || mapperContent.contains("int"),
                "UPDATE method should return int");
        assertTrue(mapperContent.contains("int deleteOrderById") || mapperContent.contains("int"),
                "DELETE method should return int");

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
        assertTrue(serviceContent.contains("tOrderMapper.updateStatusById"), "Should call mapper.updateStatusById");
        assertTrue(serviceContent.contains("tOrderMapper.updateAmountAndRemarkById"),
                "Should call mapper.updateAmountAndRemarkById");
        assertTrue(serviceContent.contains("tOrderMapper.deleteOrderById"), "Should call mapper.deleteOrderById");
        assertTrue(serviceContent.contains("tOrderMapper.deleteByUserId"), "Should call mapper.deleteByUserId");
    }

}
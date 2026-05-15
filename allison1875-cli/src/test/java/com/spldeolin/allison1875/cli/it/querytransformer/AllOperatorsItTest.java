package com.spldeolin.allison1875.cli.it.querytransformer;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import org.junit.jupiter.api.Test;

/**
 * query-transformer all-operators 集成测试。
 *
 * <p>验证全部 11 种比较运算符的 SQL 生成：
 * 1. eq(=) / ne(!=) / gt(&gt;) / ge(&gt;=) / lt(&lt;) / le(&lt;=)
 * 2. like(LIKE CONCAT) / in(IN foreach) / nin(NOT IN foreach)
 * 3. notnull(IS NOT NULL) / isnull(IS NULL)
 * 4. Mapper 接口中追加所有方法
 * 5. Service 中 Design 链全部替换为 Mapper 调用
 *
 * @author Deolin 2026-05-15
 */
public class AllOperatorsItTest extends QueryTransformerItBaseTest {

    @Test
    void test() throws IOException {
        runQueryTransformer("all-operators");

        // ========== 1. Mapper XML 验证 ==========
        File mapperXmlFile = new File(basedir, "src/main/resources/mapper/TOrderMapper.xml");
        assertTrue(mapperXmlFile.exists(), "TOrderMapper.xml should exist");
        String xmlContent = new String(Files.readAllBytes(mapperXmlFile.toPath()), StandardCharsets.UTF_8);

        // --- eq: = ---
        assertTrue(xmlContent.contains("id='queryByEq'") || xmlContent.contains("id=\"queryByEq\""),
                "XML should contain queryByEq");
        assertTrue(xmlContent.contains("id = #{id}") || xmlContent.contains("id = #{id,jdbcType=BIGINT}"),
                "eq should generate '= #{id}'");

        // --- ne: != ---
        assertTrue(xmlContent.contains("id='queryByNe'") || xmlContent.contains("id=\"queryByNe\""),
                "XML should contain queryByNe");
        assertTrue(xmlContent.contains("status != #{status}"), "ne should generate '!= #{status}'");

        // --- gt: > ---
        assertTrue(xmlContent.contains("id='queryByGt'") || xmlContent.contains("id=\"queryByGt\""),
                "XML should contain queryByGt");
        assertTrue(xmlContent.contains("amount > #{amount}") || xmlContent.contains("amount > #{minAmount}"),
                "gt should generate '> #{var}'");

        // --- ge: >= ---
        assertTrue(xmlContent.contains("id='queryByGe'") || xmlContent.contains("id=\"queryByGe\""),
                "XML should contain queryByGe");
        assertTrue(xmlContent.contains("amount >= #{amount}") || xmlContent.contains("amount >= #{minAmount}"),
                "ge should generate '>= #{var}'");

        // --- lt: < (XML escaped as &lt;) ---
        assertTrue(xmlContent.contains("id='queryByLt'") || xmlContent.contains("id=\"queryByLt\""),
                "XML should contain queryByLt");
        assertTrue(xmlContent.contains("amount &lt; #{amount}") || xmlContent.contains("amount &lt; #{maxAmount}"),
                "lt should generate '< #{var}' (XML escaped)");

        // --- le: <= (XML escaped as &lt;=) ---
        assertTrue(xmlContent.contains("id='queryByLe'") || xmlContent.contains("id=\"queryByLe\""),
                "XML should contain queryByLe");
        assertTrue(xmlContent.contains("amount &lt;= #{amount}") || xmlContent.contains("amount &lt;= #{maxAmount}"),
                "le should generate '<= #{var}' (XML escaped)");

        // --- like: LIKE CONCAT ---
        assertTrue(xmlContent.contains("id='queryByLike'") || xmlContent.contains("id=\"queryByLike\""),
                "XML should contain queryByLike");
        assertTrue(xmlContent.contains("LIKE CONCAT('%',"), "like should generate LIKE CONCAT pattern");
        assertTrue(xmlContent.contains("order_no"), "like should reference order_no column");

        // --- in: IN (<foreach>) ---
        assertTrue(xmlContent.contains("id='queryByIn'") || xmlContent.contains("id=\"queryByIn\""),
                "XML should contain queryByIn");
        assertTrue(xmlContent.contains("status IN ("), "in should generate 'column IN (...)'");
        assertTrue(xmlContent.contains("foreach"), "in should use foreach for collection");

        // --- nin: NOT IN (<foreach>) ---
        assertTrue(xmlContent.contains("id='queryByNin'") || xmlContent.contains("id=\"queryByNin\""),
                "XML should contain queryByNin");
        assertTrue(xmlContent.contains("status NOT IN ("), "nin should generate 'column NOT IN (...)'");

        // --- notnull: IS NOT NULL ---
        assertTrue(xmlContent.contains("id='queryByNotnull'") || xmlContent.contains("id=\"queryByNotnull\""),
                "XML should contain queryByNotnull");
        assertTrue(xmlContent.contains("remark IS NOT NULL"), "notnull should generate 'IS NOT NULL'");

        // --- isnull: IS NULL ---
        assertTrue(xmlContent.contains("id='queryByIsnull'") || xmlContent.contains("id=\"queryByIsnull\""),
                "XML should contain queryByIsnull");
        assertTrue(xmlContent.contains("remark IS NULL"), "isnull should generate 'IS NULL'");

        // ========== 2. Mapper 接口验证 ==========
        File mapperFile = new File(basedir, "src/main/java/com/example/mapper/TOrderMapper.java");
        assertTrue(mapperFile.exists(), "TOrderMapper.java should exist");
        String mapperContent = new String(Files.readAllBytes(mapperFile.toPath()), StandardCharsets.UTF_8);

        assertTrue(mapperContent.contains("queryByEq"), "Mapper should contain queryByEq method");
        assertTrue(mapperContent.contains("queryByNe"), "Mapper should contain queryByNe method");
        assertTrue(mapperContent.contains("queryByGt"), "Mapper should contain queryByGt method");
        assertTrue(mapperContent.contains("queryByGe"), "Mapper should contain queryByGe method");
        assertTrue(mapperContent.contains("queryByLt"), "Mapper should contain queryByLt method");
        assertTrue(mapperContent.contains("queryByLe"), "Mapper should contain queryByLe method");
        assertTrue(mapperContent.contains("queryByLike"), "Mapper should contain queryByLike method");
        assertTrue(mapperContent.contains("queryByIn"), "Mapper should contain queryByIn method");
        assertTrue(mapperContent.contains("queryByNin"), "Mapper should contain queryByNin method");
        assertTrue(mapperContent.contains("queryByNotnull"), "Mapper should contain queryByNotnull method");
        assertTrue(mapperContent.contains("queryByIsnull"), "Mapper should contain queryByIsnull method");

        // ========== 3. Service 文件验证 ==========
        File serviceFile = new File(basedir, "src/main/java/com/example/service/OrderService.java");
        assertTrue(serviceFile.exists(), "OrderService.java should exist");
        String serviceContent = new String(Files.readAllBytes(serviceFile.toPath()), StandardCharsets.UTF_8);

        // Design 链已被替换
        assertFalse(serviceContent.contains("TOrderDesign."), "Design chain should be replaced");

        // Mapper 被注入
        assertTrue(serviceContent.contains("TOrderMapper"), "Mapper should be injected");
        assertTrue(serviceContent.contains("tOrderMapper"), "Mapper field should exist");

        // 所有方法调用
        assertTrue(serviceContent.contains("tOrderMapper.queryByEq"), "Should call mapper.queryByEq");
        assertTrue(serviceContent.contains("tOrderMapper.queryByNe"), "Should call mapper.queryByNe");
        assertTrue(serviceContent.contains("tOrderMapper.queryByGt"), "Should call mapper.queryByGt");
        assertTrue(serviceContent.contains("tOrderMapper.queryByGe"), "Should call mapper.queryByGe");
        assertTrue(serviceContent.contains("tOrderMapper.queryByLt"), "Should call mapper.queryByLt");
        assertTrue(serviceContent.contains("tOrderMapper.queryByLe"), "Should call mapper.queryByLe");
        assertTrue(serviceContent.contains("tOrderMapper.queryByLike"), "Should call mapper.queryByLike");
        assertTrue(serviceContent.contains("tOrderMapper.queryByIn"), "Should call mapper.queryByIn");
        assertTrue(serviceContent.contains("tOrderMapper.queryByNin"), "Should call mapper.queryByNin");
        assertTrue(serviceContent.contains("tOrderMapper.queryByNotnull"), "Should call mapper.queryByNotnull");
        assertTrue(serviceContent.contains("tOrderMapper.queryByIsnull"), "Should call mapper.queryByIsnull");
    }

}
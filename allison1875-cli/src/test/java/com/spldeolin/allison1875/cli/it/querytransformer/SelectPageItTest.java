package com.spldeolin.allison1875.cli.it.querytransformer;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import org.junit.jupiter.api.Test;

/**
 * query-transformer select-page 集成测试。
 *
 * <p>验证分页查询功能：
 * 1. Mapper XML 中生成 pageAll/countPageAll/pageByUserId/countPageByUserId 等分页相关 SQL
 * 2. Mapper 接口中包含 offset/limit 参数和 long 返回类型
 * 3. Service 文件中 Design 链被替换为 Mapper 调用
 *
 * @author Deolin 2026-05-15
 */
public class SelectPageItTest extends QueryTransformerItBaseTest {

    @Test
    void test() throws IOException {
        runQueryTransformer("select-page");

        // ========== 1. Mapper XML 验证 ==========
        File mapperXmlFile = new File(basedir, "src/main/resources/mapper/TOrderMapper.xml");
        assertTrue(mapperXmlFile.exists(), "TOrderMapper.xml should exist");
        String xmlContent = new String(Files.readAllBytes(mapperXmlFile.toPath()), StandardCharsets.UTF_8);

        // pageAll: 无条件分页
        assertTrue(xmlContent.contains("id='pageAll'") || xmlContent.contains("id=\"pageAll\""),
                "XML should contain pageAll select");

        // 分页查询应包含 LIMIT #{offset}, #{limit}
        assertTrue(xmlContent.contains("LIMIT #{offset}, #{limit}") || xmlContent.contains("LIMIT #{offset},#{limit}"),
                "page() should generate LIMIT #{offset}, #{limit}");

        // countPageAll: pageAll 对应的 count 方法
        assertTrue(xmlContent.contains("id='countPageAll'") || xmlContent.contains("id=\"countPageAll\"")
                        || xmlContent.contains("id='countAll'") || xmlContent.contains("id=\"countAll\""),
                "XML should contain count method for pageAll");

        // pageByUserId: 带条件分页
        assertTrue(xmlContent.contains("id='pageByUserId'") || xmlContent.contains("id=\"pageByUserId\""),
                "XML should contain pageByUserId select");
        assertTrue(xmlContent.contains("user_id"), "XML should contain user_id column for pageByUserId condition");

        // countPageByUserId: pageByUserId 对应的 count 方法
        assertTrue(xmlContent.contains("id='countPageByUserId'") || xmlContent.contains("id=\"countPageByUserId\"")
                || xmlContent.contains("countByUserId"), "XML should contain count method for pageByUserId");

        // count 方法应包含 SELECT COUNT(*)
        assertTrue(xmlContent.contains("SELECT COUNT(*)"), "count methods should have SELECT COUNT(*)");

        // ========== 2. Mapper 接口验证 ==========
        File mapperFile = new File(basedir, "src/main/java/com/example/mapper/TOrderMapper.java");
        assertTrue(mapperFile.exists(), "TOrderMapper.java should exist");
        String mapperContent = new String(Files.readAllBytes(mapperFile.toPath()), StandardCharsets.UTF_8);

        assertTrue(mapperContent.contains("pageAll"), "Mapper should contain pageAll method");
        assertTrue(mapperContent.contains("pageByUserId"), "Mapper should contain pageByUserId method");
        // 分页方法应有 offset 和 limit 参数
        assertTrue(mapperContent.contains("offset"), "Page method should have offset parameter");
        assertTrue(mapperContent.contains("limit"), "Page method should have limit parameter");
        // count 方法返回 long
        assertTrue(mapperContent.contains("long"), "Count method should return long");

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
        assertTrue(serviceContent.contains("tOrderMapper.pageAll"), "Should call mapper.pageAll");
        assertTrue(serviceContent.contains("tOrderMapper.pageByUserId"), "Should call mapper.pageByUserId");
    }

}
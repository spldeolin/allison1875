package com.spldeolin.allison1875.cli.it.persistencegenerator;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import org.junit.jupiter.api.Test;

/**
 * persistence-generator existing-xml-markers 集成测试。
 *
 * @author Deolin 2026-05-14
 */
public class ExistingXmlMarkersItTest extends PersistenceGeneratorItBaseTest {

    @Test
    void test() throws IOException {
        runPersistenceGenerator("existing-xml-markers");

        File xmlFile = new File(basedir, "src/main/resources/mapper/TPaymentMapper.xml");
        assertTrue(xmlFile.exists(), "TPaymentMapper.xml should exist");
        String xmlContent = new String(Files.readAllBytes(xmlFile.toPath()), StandardCharsets.UTF_8);

        // 旧的生成内容应该被清除
        assertFalse(xmlContent.contains("oldMethod"), "Old generated content (oldMethod) should be removed");

        // 新的基础方法应该被生成
        assertTrue(xmlContent.contains("resultMap"), "New resultMap should be generated");
        assertTrue(xmlContent.contains("insert"), "New insert method should be generated");
        assertTrue(xmlContent.contains("queryById"), "New queryById method should be generated");

        // 自定义 SQL 应该被保留
        assertTrue(xmlContent.contains("customQueryByStatus"), "Custom SQL customQueryByStatus should be preserved");
        assertTrue(xmlContent.contains("SELECT * FROM t_payment WHERE status"), "Custom SQL body should be preserved");

        // [START] 标记应该存在
        assertTrue(xmlContent.contains("[START]"), "Generated content should have [START] marker");
        assertTrue(xmlContent.contains("[END]"), "Generated content should have [END] marker");
    }

}

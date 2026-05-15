package com.spldeolin.allison1875.cli.it.persistencegenerator;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import org.junit.jupiter.api.Test;

/**
 * persistence-generator all-not-null 集成测试。
 *
 * <p>验证所有字段均 NOT NULL 时，不生成 updateByIdEvenNull 方法：
 * 1. Mapper 不包含 updateByIdEvenNull，仍包含 updateById/insert/queryById/deleteById
 * 2. Mapper XML 不包含 updateByIdEvenNull
 *
 * @author Deolin 2026-05-14
 */
public class AllNotNullItTest extends PersistenceGeneratorItBaseTest {

    @Test
    void test() throws IOException {
        runPersistenceGenerator("all-not-null");

        // ========== 1. 验证 Mapper 接口 ==========
        File mapperFile = new File(basedir, "src/main/java/com/example/mapper/TStrictMapper.java");
        assertTrue(mapperFile.exists(), "TStrictMapper.java should be generated");
        String mapperContent = new String(Files.readAllBytes(mapperFile.toPath()), StandardCharsets.UTF_8);

        // 不应该生成 updateByIdEvenNull（所有字段都是 NOT NULL）
        assertFalse(mapperContent.contains("updateByIdEvenNull"),
                "Should NOT generate updateByIdEvenNull when all fields are NOT NULL");

        // 仍应生成 updateById
        assertTrue(mapperContent.contains("updateById"), "Should still generate updateById");

        // 基础方法仍应存在
        assertTrue(mapperContent.contains("insert"), "Should generate insert");
        assertTrue(mapperContent.contains("queryById"), "Should generate queryById");
        assertTrue(mapperContent.contains("deleteById"), "Should generate deleteById");

        // ========== 2. 验证 Mapper XML ==========
        File xmlFile = new File(basedir, "src/main/resources/mapper/TStrictMapper.xml");
        assertTrue(xmlFile.exists(), "TStrictMapper.xml should be generated");
        String xmlContent = new String(Files.readAllBytes(xmlFile.toPath()), StandardCharsets.UTF_8);
        assertFalse(xmlContent.contains("updateByIdEvenNull"), "XML should NOT contain updateByIdEvenNull");
    }

}

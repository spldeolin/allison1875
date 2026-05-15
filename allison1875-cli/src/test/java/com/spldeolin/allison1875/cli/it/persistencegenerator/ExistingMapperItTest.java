package com.spldeolin.allison1875.cli.it.persistencegenerator;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import org.junit.jupiter.api.Test;

/**
 * persistence-generator existing-mapper 集成测试。
 *
 * @author Deolin 2026-05-14
 */
public class ExistingMapperItTest extends PersistenceGeneratorItBaseTest {

    @Test
    void test() throws IOException {
        runPersistenceGenerator("existing-mapper");

        File mapperFile = new File(basedir, "src/main/java/com/example/mapper/TAccountMapper.java");
        assertTrue(mapperFile.exists(), "TAccountMapper.java should exist");
        String mapperContent = new String(Files.readAllBytes(mapperFile.toPath()), StandardCharsets.UTF_8);

        // 自定义方法应被保留
        assertTrue(mapperContent.contains("queryByBalanceRange"),
                "Custom method queryByBalanceRange should be preserved");
        assertTrue(mapperContent.contains("sumBalance"), "Custom method sumBalance should be preserved");

        // 基础方法应被生成
        assertTrue(mapperContent.contains("insert"), "Generated method insert should exist");
        assertTrue(mapperContent.contains("queryById"), "Generated method queryById should exist");
        assertTrue(mapperContent.contains("updateById"), "Generated method updateById should exist");
        assertTrue(mapperContent.contains("deleteById"), "Generated method deleteById should exist");
        assertTrue(mapperContent.contains("queryByAccountName"), "Generated method queryByAccountName should exist");

        // 自定义方法应在 Mapper 的末尾
        int queryByBalanceRangePos = mapperContent.indexOf("queryByBalanceRange");
        int insertPos = mapperContent.indexOf("int insert");
        assertTrue(insertPos < queryByBalanceRangePos, "Custom methods should be after generated methods");
    }

}

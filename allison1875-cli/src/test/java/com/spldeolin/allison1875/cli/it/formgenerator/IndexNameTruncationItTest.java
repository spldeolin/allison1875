package com.spldeolin.allison1875.cli.it.formgenerator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import org.junit.jupiter.api.Test;

/**
 * index-name-truncation 集成测试。
 *
 * <p>验证索引名超过 64 字符时被自动截断：
 * <ul>
 *   <li>DDL 中生成的索引名被截断到恰好 64 字符</li>
 *   <li>被截断的索引名以 {@code UNIQUE KEY} 形式出现在 DDL 中</li>
 *   <li>原始完整索引名不出现在 DDL 中</li>
 *   <li>Mapper 方法名不受索引名截断影响（基于列属性名生成）</li>
 * </ul>
 *
 * @author Deolin 2026-05-17
 */
public class IndexNameTruncationItTest extends FormGeneratorItBaseTest {

    @Test
    void test() throws IOException {
        runFormGenerator("index-name-truncation");

        // === DDL 验证：索引名截断 ===
        File ddlFile = new File(basedir, "sql/ddl.sql");
        assertTrue(ddlFile.exists(), "DDL file should be generated at sql/ddl.sql");
        String ddl = new String(Files.readAllBytes(ddlFile.toPath()), StandardCharsets.UTF_8);

        // 完整索引名（79 字符）：uk_very_long_field_name_one_very_long_field_name_two_very_long_field_name_three
        String fullIndexName = "uk_very_long_field_name_one_very_long_field_name_two_very_long_field_name_three";
        // 截断后索引名（恰好 64 字符）
        String truncatedIndexName = "uk_very_long_field_name_one_very_long_field_name_two_very_long_f";

        // 验证截断后索引名恰好 64 字符
        assertEquals(64, truncatedIndexName.length(),
                "Truncated index name should be exactly 64 characters");

        // DDL 中包含截断后的索引名
        assertTrue(ddl.contains("UNIQUE KEY `" + truncatedIndexName + "`"),
                "DDL should contain truncated index name as UNIQUE KEY");

        // DDL 中不包含原始完整索引名
        assertFalse(ddl.contains("UNIQUE KEY `" + fullIndexName + "`"),
                "DDL should NOT contain the full (79-char) index name");

        // === Mapper 接口验证：方法名不受截断影响（基于列属性名） ===
        File mapperFile = new File(basedir, "src/main/java/com/example/mapper/ConfigMapper.java");
        assertTrue(mapperFile.exists(), "ConfigMapper should be generated");
        String mapperContent = new String(Files.readAllBytes(mapperFile.toPath()), StandardCharsets.UTF_8);

        // 最左前缀 (veryLongFieldNameOne)：非唯一 → 返回 List
        assertTrue(mapperContent.contains("queryByVeryLongFieldNameOne"),
                "Mapper should contain queryByVeryLongFieldNameOne (left-prefix, not affected by truncation)");

        // 两列前缀 (veryLongFieldNameOne, veryLongFieldNameTwo)：非唯一 → 返回 List
        assertTrue(mapperContent.contains("queryByVeryLongFieldNameOneVeryLongFieldNameTwo"),
                "Mapper should contain 2-col prefix method (not affected by truncation)");

        // 完整三列 (veryLongFieldNameOne, veryLongFieldNameTwo, veryLongFieldNameThree)：唯一 → 单实体
        assertTrue(mapperContent.contains("queryByVeryLongFieldNameOneVeryLongFieldNameTwoVeryLongFieldNameThree"),
                "Mapper should contain full 3-col unique method (not affected by truncation)");

        // === 验证没有生成 api-docs 目录（enableDocAnalyzer=false） ===
        File apiDocsDir = new File(basedir, "api-docs");
        assertFalse(apiDocsDir.exists(), "api-docs directory should NOT be generated when enableDocAnalyzer=false");
    }

}

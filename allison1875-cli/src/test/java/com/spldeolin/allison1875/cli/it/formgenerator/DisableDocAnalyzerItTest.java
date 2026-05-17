package com.spldeolin.allison1875.cli.it.formgenerator;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import org.junit.jupiter.api.Test;

/**
 * disable-doc-analyzer 集成测试。
 *
 * <p>验证 {@code enableDocAnalyzer=false} 显式配置时，不调用 doc-analyzer，不生成 api-docs 目录。
 *
 * @author Deolin 2026-05-17
 */
public class DisableDocAnalyzerItTest extends FormGeneratorItBaseTest {

    @Test
    void test() throws IOException {
        runFormGenerator("disable-doc-analyzer");

        // === api-docs 目录不应生成 ===
        File apiDocsDir = new File(basedir, "api-docs");
        assertFalse(apiDocsDir.exists(), "api-docs directory should NOT be generated when enableDocAnalyzer=false");

        // === 但代码文件仍然正常生成 ===
        assertTrue(new File(basedir, "sql/ddl.sql").exists(), "DDL should still be generated");
        assertTrue(new File(basedir, "src/main/java/com/example/entity/SimpleEntity.java").exists(),
                "Entity should still be generated");
        assertTrue(new File(basedir, "src/main/java/com/example/controller/SimpleController.java").exists(),
                "Controller should still be generated");
    }

}

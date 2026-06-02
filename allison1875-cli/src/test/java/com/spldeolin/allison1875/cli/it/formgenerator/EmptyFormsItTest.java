package com.spldeolin.allison1875.cli.it.formgenerator;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import org.junit.jupiter.api.Test;

/**
 * empty-forms 集成测试。
 *
 * <p>验证 DSL 文件中表单列表为空（{@code []}）时，工具正常结束不生成文件。
 *
 * @author Deolin 2026-05-17
 */
public class EmptyFormsItTest extends FormGeneratorItBaseTest {

    @Test
    void test() {
        runFormGenerator("empty-forms");

        // === DDL 文件不应生成 ===
        assertFalse(new File(basedir, "sql/ddl.sql").exists(),
                "DDL file should NOT be generated when forms list is empty");

        // === Entity 文件不应生成 ===
        File entityDir = new File(basedir, "src/main/java/com/example/entity");
        if (entityDir.exists()) {
            File[] javaFiles = entityDir.listFiles((d, n) -> n.endsWith("Entity.java"));
            assertTrue(javaFiles == null || javaFiles.length == 0,
                    "No Entity files should be generated when forms list is empty");
        }

        // === Controller 文件不应生成 ===
        File controllerDir = new File(basedir, "src/main/java/com/example/controller");
        if (controllerDir.exists()) {
            File[] javaFiles = controllerDir.listFiles((d, n) -> n.endsWith("Controller.java"));
            assertTrue(javaFiles == null || javaFiles.length == 0,
                    "No Controller files should be generated when forms list is empty");
        }

        // === api-docs 不应生成 ===
        assertFalse(new File(basedir, "api-docs").exists(), "api-docs directory should be generated");
    }

}

package com.spldeolin.allison1875.cli.it.formgenerator;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import org.junit.jupiter.api.Test;

/**
 * enable-doc-analyzer 集成测试。
 *
 * <p>验证 {@code enableDocAnalyzer=true} 配置时，form-generator 在最后阶段调用 doc-analyzer：
 * <ul>
 *   <li>生成 api-docs 目录和 Markdown 文件</li>
 *   <li>文档内容涵盖 save/list/getDetail/delete 四个 API</li>
 *   <li>文档中包含 HTTP 方法（POST）和请求路径</li>
 * </ul>
 *
 * @author Deolin 2026-05-17
 */
public class EnableDocAnalyzerItTest extends FormGeneratorItBaseTest {

    @Test
    void test() throws IOException {
        runFormGenerator("enable-doc-analyzer");

        // ============================================================
        // === api-docs 目录验证 ===
        // ============================================================
        File apiDocsDir = new File(basedir, "api-docs");
        assertTrue(apiDocsDir.exists(), "api-docs directory should be generated when enableDocAnalyzer=true");
        assertTrue(apiDocsDir.isDirectory(), "api-docs should be a directory");

        // 查找至少一个 .md 文件
        File[] mdFiles = apiDocsDir.listFiles((dir, name) -> name.endsWith(".md"));
        assertTrue(mdFiles != null && mdFiles.length > 0,
                "At least one .md file should be generated in api-docs/");

        // 读取第一个 md 文件内容做综合验证
        String mdContent = Files.readString(mdFiles[0].toPath());

        // ============================================================
        // === 文档内容验证：五个 API ===
        // ============================================================
        // Create API
        assertTrue(mdContent.contains("createNote") || mdContent.contains("create"),
                "API doc should contain createNote API");
        // Update API
        assertTrue(mdContent.contains("updateNote") || mdContent.contains("update"),
                "API doc should contain updateNote API");
        // List API
        assertTrue(mdContent.contains("listNotes") || mdContent.contains("list"),
                "API doc should contain listNotes API");
        // GetDetail API
        assertTrue(mdContent.contains("getNoteDetail") || mdContent.contains("getDetail"),
                "API doc should contain getNoteDetail API");
        // Delete API
        assertTrue(mdContent.contains("deleteNote") || mdContent.contains("delete"),
                "API doc should contain deleteNote API");

        // ============================================================
        // === 文档格式验证 ===
        // ============================================================
        // HTTP 方法
        assertTrue(mdContent.contains("POST"), "API doc should contain HTTP method POST");
        // 请求路径
        assertTrue(mdContent.contains("/api/v1/note"), "API doc should contain request path /api/v1/note");
        // Markdown 结构
        assertTrue(mdContent.contains("#"), "API doc should contain Markdown heading");

        // ============================================================
        // === 代码生成仍然正常（form-generator 主体流程不受影响） ===
        // ============================================================
        assertTrue(new File(basedir, "sql/ddl.sql").exists(), "DDL should still be generated");
        assertTrue(new File(basedir, "src/main/java/com/example/entity/NoteEntity.java").exists(),
                "Entity should still be generated");
        assertTrue(new File(basedir, "src/main/java/com/example/controller/NoteController.java").exists(),
                "Controller should still be generated");
    }

}

package com.spldeolin.allison1875.cli.it.formgenerator;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import org.junit.jupiter.api.Test;

/**
 * cannot-input-on-edit 集成测试。
 *
 * <p>验证 (canInputOnInit=true, canInputOnEdit=false) 字段：
 * <ul>
 *   <li>该字段出现在 CreateReq DTO 中（因为 canInputOnInit=true）</li>
 *   <li>该字段不出现在 UpdateReq DTO 中（因为 canInputOnEdit=false）</li>
 *   <li>CreateServiceImpl 中包含该字段的 setter</li>
 *   <li>UpdateServiceImpl 中不包含该字段的 setter</li>
 *   <li>不再生成 // TODO 注释</li>
 * </ul>
 *
 * @author Deolin 2026-05-30
 */
public class CannotInputOnEditItTest extends FormGeneratorItBaseTest {

    @Test
    void test() throws IOException {
        runFormGenerator("cannot-input-on-edit");

        // CreateReq should contain authorName (canInputOnInit=true)
        File createReqFile = new File(basedir, "src/main/java/com/example/dto/req/CreateDocumentReq.java");
        assertTrue(createReqFile.exists(), "CreateDocumentReq DTO should be generated");
        String createReqContent = Files.readString(createReqFile.toPath());
        assertTrue(createReqContent.contains("authorName"),
                "CreateReq should contain authorName (canInputOnInit=true)");

        // UpdateReq should NOT contain authorName (canInputOnEdit=false)
        File updateReqFile = new File(basedir, "src/main/java/com/example/dto/req/UpdateDocumentReq.java");
        assertTrue(updateReqFile.exists(), "UpdateDocumentReq DTO should be generated");
        String updateReqContent = Files.readString(updateReqFile.toPath());
        assertFalse(updateReqContent.contains("authorName"),
                "UpdateReq should NOT contain authorName (canInputOnEdit=false)");

        // CreateServiceImpl should set authorName
        File createFile = new File(basedir,
                "src/main/java/com/example/service/impl/CreateDocumentServiceImpl.java");
        assertTrue(createFile.exists(), "CreateDocumentServiceImpl should be generated");
        String createContent = Files.readString(createFile.toPath());
        assertTrue(createContent.contains("document.setAuthorName("),
                "Create service should set authorName");

        // UpdateServiceImpl should NOT set authorName
        File updateFile = new File(basedir,
                "src/main/java/com/example/service/impl/UpdateDocumentServiceImpl.java");
        assertTrue(updateFile.exists(), "UpdateDocumentServiceImpl should be generated");
        String updateContent = Files.readString(updateFile.toPath());
        assertFalse(updateContent.contains("document.setAuthorName"),
                "Update service should NOT set authorName");

        // title / content are (true,true) fields, should appear in both services
        assertTrue(createContent.contains("document.setTitle(req.getTitle())"),
                "title should be set in create service");
        assertTrue(createContent.contains("document.setContent(req.getContent())"),
                "content should be set in create service");
        assertTrue(updateContent.contains("document.setTitle(req.getTitle())"),
                "title should be set in update service");
        assertTrue(updateContent.contains("document.setContent(req.getContent())"),
                "content should be set in update service");

        // No TODO comments
        assertFalse(createContent.contains("TODO 请补充"),
                "Generated code should not contain // TODO 请补充 comment");
        assertFalse(updateContent.contains("TODO 请补充"),
                "Generated code should not contain // TODO 请补充 comment");

        File apiDocsDir = new File(basedir, "api-docs");
        assertTrue(apiDocsDir.exists(), "api-docs should be generated");
    }
}

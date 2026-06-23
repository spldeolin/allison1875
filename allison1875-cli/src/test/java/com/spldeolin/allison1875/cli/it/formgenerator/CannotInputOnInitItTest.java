package com.spldeolin.allison1875.cli.it.formgenerator;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import org.junit.jupiter.api.Test;

/**
 * cannot-input-on-init 集成测试。
 *
 * <p>验证两个组合：
 * <ul>
 *   <li>(canInputOnInit=false, canInputOnEdit=true) — assigneeId（isNonVoid=false）：
 *       出现在 UpdateReq（不在 CreateReq）；UpdateServiceImpl 中有 setter</li>
 *   <li>(canInputOnInit=false, canInputOnEdit=false) — internalCode（isNonVoid=true）：
 *       不出现在任何 Req 中；CreateServiceImpl 中设置默认值</li>
 * </ul>
 *
 * @author Deolin 2026-05-30
 */
public class CannotInputOnInitItTest extends FormGeneratorItBaseTest {

    @Test
    void test() throws IOException {
        runFormGenerator("cannot-input-on-init");

        // CreateReq should contain taskName (canInputOnInit=true) but NOT assigneeId or internalCode
        File createReqFile = new File(basedir, "src/main/java/com/example/dto/req/CreateTaskReq.java");
        assertTrue(createReqFile.exists(), "CreateTaskReq DTO should be generated");
        String createReqContent = Files.readString(createReqFile.toPath());
        assertTrue(createReqContent.contains("taskName"),
                "CreateReq should contain taskName (canInputOnInit=true)");
        assertFalse(createReqContent.contains("assigneeId"),
                "CreateReq should NOT contain assigneeId (canInputOnInit=false)");
        assertFalse(createReqContent.contains("internalCode"),
                "CreateReq should NOT contain internalCode (canInputOnInit=false)");

        // UpdateReq should contain assigneeId (canInputOnEdit=true) but NOT internalCode
        File updateReqFile = new File(basedir, "src/main/java/com/example/dto/req/UpdateTaskReq.java");
        assertTrue(updateReqFile.exists(), "UpdateTaskReq DTO should be generated");
        String updateReqContent = Files.readString(updateReqFile.toPath());
        assertTrue(updateReqContent.contains("assigneeId"),
                "UpdateReq should contain assigneeId (canInputOnEdit=true)");
        assertFalse(updateReqContent.contains("internalCode"),
                "UpdateReq should NOT contain internalCode (canInputOnEdit=false)");

        // CreateServiceImpl should set internalCode with default value (canInputOnInit=false, isNonVoid=true)
        File createFile = new File(basedir,
                "src/main/java/com/example/service/impl/CreateTaskServiceImpl.java");
        assertTrue(createFile.exists(), "CreateTaskServiceImpl should be generated");
        String createContent = Files.readString(createFile.toPath());
        assertTrue(createContent.contains("task.setInternalCode("),
                "Create service should set internalCode default value");
        // assigneeId NOT set in create (canInputOnInit=false, isNonVoid=false → no default)
        assertFalse(createContent.contains("task.setAssigneeId(req.getAssigneeId())"),
                "Create service should NOT set assigneeId from req");

        // UpdateServiceImpl should set assigneeId from req (canInputOnEdit=true)
        File updateFile = new File(basedir,
                "src/main/java/com/example/service/impl/UpdateTaskServiceImpl.java");
        assertTrue(updateFile.exists(), "UpdateTaskServiceImpl should be generated");
        String updateContent = Files.readString(updateFile.toPath());
        assertTrue(updateContent.contains("task.setAssigneeId(req.getAssigneeId())"),
                "Update service should set assigneeId from req");
        // No validation throw for nullable field
        assertFalse(updateContent.contains("throw new IllegalArgumentException(\"负责人ID不能为空\")"),
                "no validation throw for nullable field");
        // internalCode NOT set in update (canInputOnEdit=false)
        assertFalse(updateContent.contains("task.setInternalCode"),
                "Update service should NOT set internalCode");

        // No TODO comments
        assertFalse(createContent.contains("TODO 请补充"),
                "Generated code should not contain // TODO 请补充 comment");
        assertFalse(updateContent.contains("TODO 请补充"),
                "Generated code should not contain // TODO 请补充 comment");

        File apiDocsDir = new File(basedir, "api-docs");
        assertTrue(apiDocsDir.exists(), "api-docs should be generated");
    }
}

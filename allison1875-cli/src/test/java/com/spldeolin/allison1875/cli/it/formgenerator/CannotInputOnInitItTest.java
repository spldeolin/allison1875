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
 *       出现在 SaveReq；setter 仅在 else 内；isNonVoid=false 故无 if-throw 与默认值赋值</li>
 *   <li>(canInputOnInit=false, canInputOnEdit=false) — internalCode（isNonVoid=true）：
 *       不出现在 SaveReq；toCreate 分支内用 getTodoValue 设置默认值；else/common 中不出现</li>
 * </ul>
 *
 * @author Deolin 2026-05-30
 */
public class CannotInputOnInitItTest extends FormGeneratorItBaseTest {

    @Test
    void test() throws IOException {
        runFormGenerator("cannot-input-on-init");

        File saveReqFile = new File(basedir, "src/main/java/com/example/dto/req/SaveTaskReq.java");
        assertTrue(saveReqFile.exists(), "SaveTaskReq DTO should be generated");
        String saveReqContent = Files.readString(saveReqFile.toPath());

        assertTrue(saveReqContent.contains("taskName"),
                "SaveReq should contain taskName (canInputOnInit=true)");
        assertTrue(saveReqContent.contains("assigneeId"),
                "SaveReq should contain assigneeId (canInputOnEdit=true)");
        assertFalse(saveReqContent.contains("internalCode"),
                "SaveReq should NOT contain internalCode (both canInputOn* are false)");

        File saveFile = new File(basedir,
                "src/main/java/com/example/service/impl/SaveTaskServiceImpl.java");
        assertTrue(saveFile.exists(), "SaveTaskServiceImpl should be generated");
        String saveContent = Files.readString(saveFile.toPath());

        int toCreateStart = saveContent.indexOf("if (toCreate) {");
        int elseStart = saveContent.indexOf("} else {");
        int insertCall = saveContent.indexOf("taskMapper.insert");

        String toCreateSection = saveContent.substring(toCreateStart, elseStart);
        String elseSection = saveContent.substring(elseStart, insertCall);

        // (false,false) + isNonVoid=true → toCreate 分支内默认值
        assertTrue(toCreateSection.contains("task.setInternalCode("),
                "toCreate should set internalCode default value");

        // (false,true) + isNonVoid=false → else 内 setter，无 if-throw
        assertTrue(elseSection.contains("task.setAssigneeId(req.getAssigneeId())"),
                "else should set assigneeId from req");
        assertFalse(elseSection.contains("throw new IllegalArgumentException(\"负责人ID不能为空\")"),
                "no validation throw for nullable field");

        // common 节（insert 之后）不出现以上两个 setter
        String afterInsert = saveContent.substring(insertCall);
        assertFalse(afterInsert.contains("task.setAssigneeId"),
                "assigneeId should NOT be set in common section");
        assertFalse(afterInsert.contains("task.setInternalCode"),
                "internalCode should NOT be set in common section");

        // 不再生成 TODO 行注释
        assertFalse(saveContent.contains("TODO 请补充"),
                "Generated code should not contain // TODO 请补充 comment");

        File apiDocsDir = new File(basedir, "api-docs");
        assertFalse(apiDocsDir.exists(), "api-docs should NOT be generated");
    }
}

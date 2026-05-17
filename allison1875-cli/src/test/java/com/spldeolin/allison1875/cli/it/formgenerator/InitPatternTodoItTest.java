package com.spldeolin.allison1875.cli.it.formgenerator;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import org.junit.jupiter.api.Test;

/**
 * init-pattern-todo 集成测试。
 *
 * <p>验证 {@code initPattern=todo} 字段的生成行为：
 * <ul>
 *   <li>该字段<b>不出现</b>在 SaveReq DTO 中（非用户输入）</li>
 *   <li>Save ServiceImpl 的 {@code if (toCreate)} 分支中包含 {@code entity.setXxx(todoValue); // TODO 请补充初始值}</li>
 *   <li>编辑分支（else）中该字段不变更（editPattern=doNot）</li>
 * </ul>
 *
 * @author Deolin 2026-05-17
 */
public class InitPatternTodoItTest extends FormGeneratorItBaseTest {

    @Test
    void test() throws IOException {
        runFormGenerator("init-pattern-todo");

        // === SaveReq DTO 验证：TODO 字段不应出现 ===
        File saveReqFile = new File(basedir, "src/main/java/com/example/dto/req/SaveTaskReq.java");
        assertTrue(saveReqFile.exists(), "SaveTaskReq DTO should be generated");
        String saveReqContent = new String(Files.readAllBytes(saveReqFile.toPath()), StandardCharsets.UTF_8);
        // 普通 USER_INPUT 字段应出现
        assertTrue(saveReqContent.contains("taskName"),
                "SaveReq should contain taskName (initPattern=userInput)");
        // initPattern=todo 的字段不应出现（非用户输入）
        assertFalse(saveReqContent.contains("assigneeId"),
                "SaveReq should NOT contain assigneeId (initPattern=todo, not user input)");

        // === Save ServiceImpl 验证：TODO 注释 + 初始化 ===
        File saveServiceImplFile = new File(basedir,
                "src/main/java/com/example/service/impl/SaveTaskServiceImpl.java");
        assertTrue(saveServiceImplFile.exists(), "SaveTaskServiceImpl should be generated");
        String saveContent = new String(Files.readAllBytes(saveServiceImplFile.toPath()), StandardCharsets.UTF_8);

        // 创建分支：包含 TODO 注释和 setAssigneeId
        assertTrue(saveContent.contains("task.setAssigneeId("),
                "Save service toCreate branch should call task.setAssigneeId()");
        assertTrue(saveContent.contains("TODO 请补充初始值"),
                "Save service should contain 'TODO 请补充初始值' comment");

        // 验证 TODO 出现在正确位置：应在 task.setTaskCode 之后、task.setCreatedAt 之前
        int todoIndex = saveContent.indexOf("TODO 请补充初始值");
        int setCodeIndex = saveContent.indexOf("task.setTaskCode(");
        int setCreatedAtIndex = saveContent.indexOf("task.setCreatedAt(");
        assertTrue(todoIndex > setCodeIndex,
                "TODO comment should appear after setTaskCode (inside toCreate branch)");
        assertTrue(todoIndex < setCreatedAtIndex,
                "TODO comment should appear before setCreatedAt");

        // 验证不含对 assigneeId 的非 TODO 设置（普通 set 不出现在 common 节）
        String afterElseBranch = saveContent.substring(saveContent.indexOf("} else {"));
        assertFalse(afterElseBranch.contains("task.setAssigneeId"),
                "assigneeId should NOT be set in else/edit branch (editPattern=doNot)");

        // === 验证没有生成 api-docs 目录 ===
        File apiDocsDir = new File(basedir, "api-docs");
        assertFalse(apiDocsDir.exists(), "api-docs directory should NOT be generated");
    }

}

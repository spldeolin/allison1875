package com.spldeolin.allison1875.cli.it.formgenerator;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import org.junit.jupiter.api.Test;

/**
 * delete-api 集成测试。
 *
 * <p>验证 Delete API 的完整生成：
 * <ul>
 *   <li>Controller 中生成 {@code deleteTask} handler（POST），Req 包含业务 ID 列表（{@code @NotEmpty}）</li>
 *   <li>DeleteReq DTO 包含 {@code List<String> taskCode} 业务 ID 列表 + {@code @NotEmpty} 校验</li>
 *   <li>Service 中 Design Chain（{@code .delete().where().bizId.in(ids).over()}）已被 query-transformer 转换为 Mapper 调用</li>
 *   <li>有 multiSelect 字段时，级联删除关联表（先删关联表，再删主表）</li>
 * </ul>
 *
 * @author Deolin 2026-05-17
 */
public class DeleteApiItTest extends FormGeneratorItBaseTest {

    @Test
    void test() throws IOException {
        runFormGenerator("delete-api");

        // ============================================================
        // === Controller 验证 ===
        // ============================================================
        File controllerFile = new File(basedir, "src/main/java/com/example/controller/TaskController.java");
        assertTrue(controllerFile.exists(), "TaskController file should be generated");
        String controllerContent = Files.readString(controllerFile.toPath());
        assertTrue(controllerContent.contains("class TaskController"), "Should declare class TaskController");
        assertTrue(controllerContent.contains("@RestController"), "Controller should have @RestController");
        // deleteTask handler
        assertTrue(controllerContent.contains("deleteTask"), "Controller should contain deleteTask handler");
        assertTrue(controllerContent.contains("DeleteTaskReq"),
                "Controller deleteTask should reference DeleteTaskReq");
        // delete API 通常返回 void
        assertTrue(controllerContent.contains("void deleteTask") || controllerContent.contains("Void deleteTask"),
                "deleteTask should return void");

        // ============================================================
        // === DeleteReq DTO 验证 ===
        // ============================================================
        File reqFile = new File(basedir, "src/main/java/com/example/dto/req/DeleteTaskReq.java");
        assertTrue(reqFile.exists(), "DeleteTaskReq DTO should be generated");
        String reqContent = Files.readString(reqFile.toPath());
        assertTrue(reqContent.contains("class DeleteTaskReq"));
        // 业务 ID 列表
        assertTrue(reqContent.contains("List<String> taskCode"),
                "DeleteReq should contain List<String> taskCode");
        // @NotEmpty 校验
        assertTrue(
                reqContent.contains("@NotEmpty") || reqContent.contains("@javax.validation.constraints.NotEmpty"),
                "DeleteReq taskCode list should have @NotEmpty");

        // ============================================================
        // === Delete Service 接口验证 ===
        // ============================================================
        File serviceFile = new File(basedir, "src/main/java/com/example/service/DeleteTaskService.java");
        assertTrue(serviceFile.exists(), "DeleteTaskService interface should be generated");
        String serviceContent = Files.readString(serviceFile.toPath());
        assertTrue(serviceContent.contains("interface DeleteTaskService"));
        assertTrue(serviceContent.contains("deleteTask"), "Service should declare deleteTask method");

        // ============================================================
        // === Delete ServiceImpl 验证 — Design Chain → Mapper 调用 ===
        // ============================================================
        File implFile = new File(basedir,
                "src/main/java/com/example/service/impl/DeleteTaskServiceImpl.java");
        assertTrue(implFile.exists(), "DeleteTaskServiceImpl should be generated");
        String implContent = Files.readString(implFile.toPath());
        assertTrue(implContent.contains("class DeleteTaskServiceImpl"));
        assertTrue(implContent.contains("implements DeleteTaskService"));

        // 关联表级联删除（先删关联表）
        assertTrue(implContent.contains("taskTagsMapper.deleteTaskTags("),
                "Delete service should cascade delete association table via taskTagsMapper.deleteTaskTags");

        // 主表删除
        assertTrue(implContent.contains("taskMapper.deleteTask("),
                "Delete service should delete from main table via taskMapper.deleteTask");

        // ============================================================
        // === Mapper 验证 ===
        // ============================================================
        // 主表 Mapper
        File taskMapperFile = new File(basedir, "src/main/java/com/example/mapper/TaskMapper.java");
        assertTrue(taskMapperFile.exists(), "TaskMapper interface should be generated");
        String taskMapperContent = Files.readString(taskMapperFile.toPath());
        assertTrue(taskMapperContent.contains("deleteTask"), "TaskMapper should contain deleteTask method");

        // 关联表 Mapper
        File associationMapperFile = new File(basedir,
                "src/main/java/com/example/mapper/TaskTagsMapper.java");
        assertTrue(associationMapperFile.exists(), "TaskTagsMapper interface should be generated");
        String associationMapperContent = Files.readString(associationMapperFile.toPath());
        assertTrue(associationMapperContent.contains("deleteTaskTags"),
                "Association Mapper should contain deleteTaskTags method");

        // ============================================================
        // === Mapper XML 验证 ===
        // ============================================================
        File taskXmlFile = new File(basedir, "src/main/resources/mapper/TaskMapper.xml");
        assertTrue(taskXmlFile.exists(), "TaskMapper XML should be generated");
        String taskXmlContent = Files.readString(taskXmlFile.toPath());
        assertTrue(taskXmlContent.contains("deleteTask"), "TaskMapper XML should contain deleteTask statement");

        File associationXmlFile = new File(basedir, "src/main/resources/mapper/TaskTagsMapper.xml");
        assertTrue(associationXmlFile.exists(), "TaskTagsMapper XML should be generated");
        String associationXmlContent = Files.readString(associationXmlFile.toPath());
        assertTrue(associationXmlContent.contains("deleteTaskTags"),
                "TaskTagsMapper XML should contain deleteTaskTags statement");

        // ============================================================
        // === 验证没有生成 api-docs 目录 ===
        // ============================================================
        File apiDocsDir = new File(basedir, "api-docs");
        assertFalse(apiDocsDir.exists(), "api-docs directory should NOT be generated");
    }

}

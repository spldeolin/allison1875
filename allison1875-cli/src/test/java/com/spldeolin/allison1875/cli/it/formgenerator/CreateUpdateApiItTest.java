package com.spldeolin.allison1875.cli.it.formgenerator;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import org.junit.jupiter.api.Test;

/**
 * create/update API 集成测试。
 *
 * <p>验证 Save API 拆分为 Create + Update 后的完整生成：
 * <ul>
 *   <li>Controller 中生成 {@code createItem} 和 {@code updateItem} 两个 handler</li>
 *   <li>CreateReq 只含 canInputOnInit=true 字段（无 bizId），CreateResp 含 bizId</li>
 *   <li>UpdateReq 含 bizId + canInputOnEdit=true 字段，无 Resp</li>
 *   <li>CreateServiceImpl：new Entity → setBizId → setCreatedAt → setUpdatedAt → insert</li>
 *   <li>UpdateServiceImpl：queryByBizId → null check → setters → setUpdatedAt → updateById</li>
 * </ul>
 *
 * @author Deolin 2026-06-23
 */
public class CreateUpdateApiItTest extends FormGeneratorItBaseTest {

    @Test
    void test() throws IOException {
        runFormGenerator("create-update-api");

        // ============================================================
        // === Controller 验证 ===
        // ============================================================
        File controllerFile = new File(basedir, "src/main/java/com/example/controller/ItemController.java");
        assertTrue(controllerFile.exists(), "ItemController file should be generated");
        String controllerContent = Files.readString(controllerFile.toPath());
        assertTrue(controllerContent.contains("createItem"), "Controller should contain createItem handler");
        assertTrue(controllerContent.contains("updateItem"), "Controller should contain updateItem handler");
        assertFalse(controllerContent.contains("saveItem"), "Controller should NOT contain saveItem handler");

        // ============================================================
        // === CreateReq DTO 验证 ===
        // ============================================================
        File createReqFile = new File(basedir, "src/main/java/com/example/dto/req/CreateItemReq.java");
        assertTrue(createReqFile.exists(), "CreateItemReq DTO should be generated");
        String createReqContent = Files.readString(createReqFile.toPath());
        assertTrue(createReqContent.contains("class CreateItemReq"));
        // No bizId in create request
        assertFalse(createReqContent.contains("itemCode"), "CreateReq should NOT contain itemCode");
        // User fields
        assertTrue(createReqContent.contains("itemName"), "CreateReq should contain itemName");
        assertTrue(createReqContent.contains("quantity"), "CreateReq should contain quantity");
        // Validation annotations directly on fields
        assertTrue(createReqContent.contains("@NotBlank"), "nonVoid text should have @NotBlank");
        assertTrue(createReqContent.contains("@NotNull"), "nonVoid number/onOff should have @NotNull");

        // ============================================================
        // === CreateResp DTO 验证 ===
        // ============================================================
        File createRespFile = new File(basedir, "src/main/java/com/example/dto/resp/CreateItemResp.java");
        assertTrue(createRespFile.exists(), "CreateItemResp DTO should be generated");
        String createRespContent = Files.readString(createRespFile.toPath());
        assertTrue(createRespContent.contains("class CreateItemResp"));
        assertTrue(createRespContent.contains("itemCode"), "CreateResp should contain itemCode (return bizId)");

        // ============================================================
        // === UpdateReq DTO 验证 ===
        // ============================================================
        File updateReqFile = new File(basedir, "src/main/java/com/example/dto/req/UpdateItemReq.java");
        assertTrue(updateReqFile.exists(), "UpdateItemReq DTO should be generated");
        String updateReqContent = Files.readString(updateReqFile.toPath());
        assertTrue(updateReqContent.contains("class UpdateItemReq"));
        // bizId present in update request
        assertTrue(updateReqContent.contains("itemCode"), "UpdateReq should contain itemCode as bizId");
        // User fields
        assertTrue(updateReqContent.contains("itemName"), "UpdateReq should contain itemName");

        // ============================================================
        // === Create ServiceImpl 验证 ===
        // ============================================================
        File createServiceImplFile = new File(basedir,
                "src/main/java/com/example/service/impl/CreateItemServiceImpl.java");
        assertTrue(createServiceImplFile.exists(), "CreateItemServiceImpl should be generated");
        String createContent = Files.readString(createServiceImplFile.toPath());
        assertTrue(createContent.contains("class CreateItemServiceImpl"));
        assertTrue(createContent.contains("new ItemEntity()"), "Create should instantiate new entity");
        assertTrue(createContent.contains("UUID.randomUUID()"), "Create should generate UUID for bizId");
        assertTrue(createContent.contains("setCreatedAt("), "Create should set createdAt");
        assertTrue(createContent.contains("setUpdatedAt("), "Create should set updatedAt");
        assertTrue(createContent.contains("itemMapper.insert("), "Create should call insert");
        assertFalse(createContent.contains("toCreate"), "Create should NOT have toCreate branch logic");
        assertTrue(createContent.contains("new CreateItemResp()"), "Create should return CreateItemResp");

        // ============================================================
        // === Update ServiceImpl 验证 ===
        // ============================================================
        File updateServiceImplFile = new File(basedir,
                "src/main/java/com/example/service/impl/UpdateItemServiceImpl.java");
        assertTrue(updateServiceImplFile.exists(), "UpdateItemServiceImpl should be generated");
        String updateContent = Files.readString(updateServiceImplFile.toPath());
        assertTrue(updateContent.contains("class UpdateItemServiceImpl"));
        assertTrue(updateContent.contains("itemMapper.queryByItemCode("), "Update should query by bizId");
        assertTrue(updateContent.contains("RuntimeException"), "Update should throw if not found");
        assertTrue(updateContent.contains("setUpdatedAt("), "Update should set updatedAt");
        assertTrue(updateContent.contains("itemMapper.updateById("), "Update should call updateById");
        assertFalse(updateContent.contains("toCreate"), "Update should NOT have toCreate branch logic");
        assertFalse(updateContent.contains("new ItemEntity()"), "Update should NOT create new entity");

        // ============================================================
        // === 验证不存在 Save 相关文件 ===
        // ============================================================
        File saveReqFile = new File(basedir, "src/main/java/com/example/dto/req/SaveItemReq.java");
        assertFalse(saveReqFile.exists(), "SaveItemReq should NOT exist");
        File saveServiceFile = new File(basedir, "src/main/java/com/example/service/SaveItemService.java");
        assertFalse(saveServiceFile.exists(), "SaveItemService should NOT exist");
    }

}

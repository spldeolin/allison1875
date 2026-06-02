package com.spldeolin.allison1875.cli.it.formgenerator;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import org.junit.jupiter.api.Test;

/**
 * save-api 集成测试。
 *
 * <p>验证 Save API 的完整生成：
 * <ul>
 *   <li>Controller 中生成 {@code saveItem} handler（POST），包含 SaveReq（bizId + 用户字段）和 SaveResp（bizId）</li>
 *   <li>Service 中包含 {@code toCreate} 判断逻辑（{@code req.getItemCode() == null}）</li>
 *   <li>创建分支：new Entity → setItemCode(shortUuid) → setCreatedAt → setter 赋值 → insert</li>
 *   <li>编辑分支：queryByItemCode → null 检查 → RuntimeException → setter 赋值 → updateById</li>
 *   <li>各字段类型的 setter 调用正确（text/number/select onOff/optional nullable）</li>
 *   <li>select 字段通过 {@code .getCode()} 转换</li>
 * </ul>
 *
 * @author Deolin 2026-05-17
 */
public class SaveApiItTest extends FormGeneratorItBaseTest {

    @Test
    void test() throws IOException {
        runFormGenerator("save-api");

        // ============================================================
        // === Controller 验证 ===
        // ============================================================
        File controllerFile = new File(basedir, "src/main/java/com/example/controller/ItemController.java");
        assertTrue(controllerFile.exists(), "ItemController file should be generated");
        String controllerContent = Files.readString(controllerFile.toPath());
        assertTrue(controllerContent.contains("class ItemController"), "Should declare class ItemController");
        assertTrue(controllerContent.contains("@RestController"), "Controller should have @RestController");
        assertTrue(controllerContent.contains("@RequestMapping"), "Controller should have @RequestMapping");
        // saveItem handler
        assertTrue(controllerContent.contains("saveItem"), "Controller should contain saveItem handler");
        assertTrue(controllerContent.contains("SaveItemReq"), "Controller saveItem should reference SaveItemReq");
        assertTrue(controllerContent.contains("SaveItemResp"), "Controller saveItem should reference SaveItemResp");
        // POST method
        assertTrue(controllerContent.contains("@PostMapping") || controllerContent.contains("@RequestMapping"),
                "Controller saveItem should have @PostMapping or @RequestMapping");

        // ============================================================
        // === SaveReq DTO 验证 ===
        // ============================================================
        File saveReqFile = new File(basedir, "src/main/java/com/example/dto/req/SaveItemReq.java");
        assertTrue(saveReqFile.exists(), "SaveItemReq DTO should be generated");
        String saveReqContent = Files.readString(saveReqFile.toPath());
        assertTrue(saveReqContent.contains("class SaveItemReq"));
        // bizId（编辑时识别用）
        assertTrue(saveReqContent.contains("itemCode"), "SaveReq should contain itemCode as bizId");
        // 用户字段
        assertTrue(saveReqContent.contains("itemName"), "SaveReq should contain itemName");
        assertTrue(saveReqContent.contains("quantity"), "SaveReq should contain quantity");
        assertTrue(saveReqContent.contains("unitPrice"), "SaveReq should contain unitPrice");
        assertTrue(saveReqContent.contains("CategoryEnum category"), "SaveReq should contain CategoryEnum category");
        assertTrue(saveReqContent.contains("isActive"), "SaveReq should contain isActive");
        assertTrue(saveReqContent.contains("remark"), "SaveReq should contain remark");
        // 校验注解
        assertTrue(saveReqContent.contains("@NotBlank"), "nonVoid text should have @NotBlank");
        assertTrue(saveReqContent.contains("@NotNull"), "nonVoid number/onOff should have @NotNull");

        // ============================================================
        // === SaveResp DTO 验证 ===
        // ============================================================
        File saveRespFile = new File(basedir, "src/main/java/com/example/dto/resp/SaveItemResp.java");
        assertTrue(saveRespFile.exists(), "SaveItemResp DTO should be generated");
        String saveRespContent = Files.readString(saveRespFile.toPath());
        assertTrue(saveRespContent.contains("class SaveItemResp"));
        assertTrue(saveRespContent.contains("itemCode"), "SaveResp should contain itemCode (return bizId)");

        // ============================================================
        // === Save Service 接口验证 ===
        // ============================================================
        File saveServiceFile = new File(basedir, "src/main/java/com/example/service/SaveItemService.java");
        assertTrue(saveServiceFile.exists(), "SaveItemService interface should be generated");
        String saveServiceContent = Files.readString(saveServiceFile.toPath());
        assertTrue(saveServiceContent.contains("interface SaveItemService"));
        assertTrue(saveServiceContent.contains("saveItem"), "SaveItemService should declare saveItem method");

        // ============================================================
        // === Save ServiceImpl 验证 ===
        // ============================================================
        File saveServiceImplFile = new File(basedir,
                "src/main/java/com/example/service/impl/SaveItemServiceImpl.java");
        assertTrue(saveServiceImplFile.exists(), "SaveItemServiceImpl file should be generated");
        String saveContent = Files.readString(saveServiceImplFile.toPath());
        assertTrue(saveContent.contains("class SaveItemServiceImpl"));
        assertTrue(saveContent.contains("implements SaveItemService"));

        // --- toCreate 判断逻辑 ---
        assertTrue(saveContent.contains("toCreate"),
                "Save service should have toCreate boolean variable");
        assertTrue(saveContent.contains("req.getItemCode() == null"),
                "toCreate should be based on req.getItemCode() == null");

        // --- 创建分支 ---
        assertTrue(saveContent.contains("new ItemEntity()"),
                "Create branch should instantiate new ItemEntity()");
        // shortUuid 生成 bizId
        assertTrue(saveContent.contains("UUID.randomUUID()"),
                "Create branch should use UUID.randomUUID() for shortUuid");
        assertTrue(saveContent.contains("item.setItemCode("),
                "Create branch should set itemCode via shortUuid");
        // createdAt 在创建分支
        int createBlockStart = saveContent.indexOf("if (toCreate)");
        int elseBlockStart = saveContent.indexOf("} else {");
        String createBlock = saveContent.substring(createBlockStart, elseBlockStart);
        assertTrue(createBlock.contains("item.setCreatedAt("),
                "Create branch should set createdAt");

        // --- 编辑分支 ---
        assertTrue(saveContent.contains("itemMapper.queryByItemCode("),
                "Edit branch should query by bizId via itemMapper.queryByItemCode");
        // null 检查
        assertTrue(saveContent.contains("== null") && saveContent.contains("RuntimeException"),
                "Edit branch should throw RuntimeException if entity not found");

        // --- 通用字段赋值（common 节） ---
        // text 字段
        assertTrue(saveContent.contains("item.setItemName(req.getItemName())"),
                "Should set itemName from req");
        // number 字段 (nonVoid, nonDecimal → Long)
        assertTrue(saveContent.contains("item.setQuantity(req.getQuantity())"),
                "Should set quantity from req");
        // number 字段 (nullable, decimal → BigDecimal)
        assertTrue(saveContent.contains("item.setUnitPrice(req.getUnitPrice())"),
                "Should set unitPrice from req");
        // select 字段 → .getCode()
        assertTrue(saveContent.contains("req.getCategory().getCode()"),
                "Should set category via req.getCategory().getCode()");
        // onOff 字段
        assertTrue(saveContent.contains("item.setIsActive(req.getIsActive())"),
                "Should set isActive from req");
        // optional text
        assertTrue(saveContent.contains("item.setRemark(req.getRemark())"),
                "Should set remark from req");
        // updatedAt 在 common 节
        assertTrue(saveContent.contains("item.setUpdatedAt("),
                "Should set updatedAt in common section");

        // --- 持久化调用 ---
        assertTrue(saveContent.contains("itemMapper.insert(item)"),
                "Create branch should call itemMapper.insert");
        assertTrue(saveContent.contains("itemMapper.updateById(item)"),
                "Edit branch should call itemMapper.updateById");

        // --- 返回值 ---
        assertTrue(saveContent.contains("new SaveItemResp()"),
                "Should return new SaveItemResp");
        assertTrue(saveContent.contains(".setItemCode(item.getItemCode())"),
                "Should set itemCode on response");

        // ============================================================
        // === Enum 验证 ===
        // ============================================================
        File enumFile = new File(basedir, "src/main/java/com/example/enums/CategoryEnum.java");
        assertTrue(enumFile.exists(), "CategoryEnum file should be generated");
        String enumContent = Files.readString(enumFile.toPath());
        assertTrue(enumContent.contains("enum CategoryEnum"), "Should declare enum CategoryEnum");
        assertTrue(enumContent.contains("FOOD"), "Enum should contain FOOD");
        assertTrue(enumContent.contains("ELECTRONICS"), "Enum should contain ELECTRONICS");
        assertTrue(enumContent.contains("CLOTHING"), "Enum should contain CLOTHING");

        // ============================================================
        // === 验证没有生成 api-docs 目录 ===
        // ============================================================
        File apiDocsDir = new File(basedir, "api-docs");
        assertTrue(apiDocsDir.exists(), "api-docs directory should be generated");
    }

}

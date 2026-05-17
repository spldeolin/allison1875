package com.spldeolin.allison1875.cli.it.formgenerator;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import org.junit.jupiter.api.Test;

/**
 * get-detail-api 集成测试。
 *
 * <p>验证 GetDetail API 的完整生成：
 * <ul>
 *   <li>Controller 中生成 {@code getItemDetail} handler（POST），Req 包含业务 ID（{@code @NotNull}）</li>
 *   <li>Resp 包含所有非 secret 字段</li>
 *   <li>Service 按业务主键查询（{@code queryByItemCode}）→ null 检查 → RuntimeException</li>
 *   <li>字段转换：select → {@code CategoryEnum.of()}，各字段 set 到 Resp DTO</li>
 * </ul>
 *
 * @author Deolin 2026-05-17
 */
public class GetDetailApiItTest extends FormGeneratorItBaseTest {

    @Test
    void test() throws IOException {
        runFormGenerator("get-detail-api");

        // ============================================================
        // === Controller 验证 ===
        // ============================================================
        File controllerFile = new File(basedir, "src/main/java/com/example/controller/ItemController.java");
        assertTrue(controllerFile.exists(), "ItemController file should be generated");
        String controllerContent = Files.readString(controllerFile.toPath());
        assertTrue(controllerContent.contains("class ItemController"), "Should declare class ItemController");
        assertTrue(controllerContent.contains("@RestController"), "Controller should have @RestController");
        // getItemDetail handler
        assertTrue(controllerContent.contains("getItemDetail"), "Controller should contain getItemDetail handler");
        assertTrue(controllerContent.contains("GetItemDetailReq"),
                "Controller getItemDetail should reference GetItemDetailReq");
        assertTrue(controllerContent.contains("GetItemDetailResp"),
                "Controller getItemDetail should reference GetItemDetailResp");

        // ============================================================
        // === GetDetailReq DTO 验证 ===
        // ============================================================
        File reqFile = new File(basedir, "src/main/java/com/example/dto/req/GetItemDetailReq.java");
        assertTrue(reqFile.exists(), "GetItemDetailReq DTO should be generated");
        String reqContent = Files.readString(reqFile.toPath());
        assertTrue(reqContent.contains("class GetItemDetailReq"));
        // 业务主键：itemCode + @NotNull
        assertTrue(reqContent.contains("itemCode"), "GetDetailReq should contain itemCode");
        assertTrue(
                reqContent.contains("@NotNull") || reqContent.contains("@javax.validation.constraints.NotNull"),
                "GetDetailReq itemCode should have @NotNull");

        // ============================================================
        // === GetDetailResp DTO 验证 ===
        // ============================================================
        File respFile = new File(basedir, "src/main/java/com/example/dto/resp/GetItemDetailResp.java");
        assertTrue(respFile.exists(), "GetItemDetailResp DTO should be generated");
        String respContent = Files.readString(respFile.toPath());
        assertTrue(respContent.contains("class GetItemDetailResp"));

        // 包含所有非 secret 字段
        assertTrue(respContent.contains("itemCode"), "GetDetailResp should contain itemCode");
        assertTrue(respContent.contains("itemName"), "GetDetailResp should contain itemName");
        assertTrue(respContent.contains("quantity"), "GetDetailResp should contain quantity");
        assertTrue(respContent.contains("unitPrice"), "GetDetailResp should contain unitPrice");
        // select 字段 → 枚举类型
        assertTrue(respContent.contains("CategoryEnum category"), "GetDetailResp should contain CategoryEnum category");
        assertTrue(respContent.contains("isActive"), "GetDetailResp should contain isActive");
        assertTrue(respContent.contains("remark"), "GetDetailResp should contain remark");
        assertTrue(respContent.contains("createdAt"), "GetDetailResp should contain createdAt");
        assertTrue(respContent.contains("updatedAt"), "GetDetailResp should contain updatedAt");

        // ============================================================
        // === GetDetail Service 接口验证 ===
        // ============================================================
        File serviceFile = new File(basedir, "src/main/java/com/example/service/GetItemDetailService.java");
        assertTrue(serviceFile.exists(), "GetItemDetailService interface should be generated");
        String serviceContent = Files.readString(serviceFile.toPath());
        assertTrue(serviceContent.contains("interface GetItemDetailService"));
        assertTrue(serviceContent.contains("getItemDetail"), "Service should declare getItemDetail method");

        // ============================================================
        // === GetDetail ServiceImpl 验证 ===
        // ============================================================
        File implFile = new File(basedir,
                "src/main/java/com/example/service/impl/GetItemDetailServiceImpl.java");
        assertTrue(implFile.exists(), "GetItemDetailServiceImpl should be generated");
        String implContent = Files.readString(implFile.toPath());
        assertTrue(implContent.contains("class GetItemDetailServiceImpl"));
        assertTrue(implContent.contains("implements GetItemDetailService"));

        // 按业务主键查询
        assertTrue(implContent.contains("itemMapper.queryByItemCode("),
                "GetDetail service should call itemMapper.queryByItemCode by bizId");
        assertTrue(implContent.contains("req.getItemCode()"),
                "GetDetail service should pass req.getItemCode() to query");

        // null 检查 + 异常
        assertTrue(implContent.contains("== null"),
                "GetDetail service should null-check the query result");
        assertTrue(implContent.contains("RuntimeException") || implContent.contains("throw new"),
                "GetDetail service should throw exception if not found");

        // 字段转换
        // select 字段 → CategoryEnum.of()
        assertTrue(implContent.contains("CategoryEnum.of("),
                "GetDetail service should call CategoryEnum.of() for select field");
        assertTrue(implContent.contains("item.getCategory()"),
                "GetDetail service should pass item.getCategory() to CategoryEnum.of()");

        // 各字段 set 到 Resp
        assertTrue(implContent.contains("result.setItemCode("),
                "Should set itemCode on result");
        assertTrue(implContent.contains("result.setItemName("),
                "Should set itemName on result");
        assertTrue(implContent.contains("result.setQuantity("),
                "Should set quantity on result");
        assertTrue(implContent.contains("result.setUnitPrice("),
                "Should set unitPrice on result");
        assertTrue(implContent.contains("result.setCategory("),
                "Should set category on result");
        assertTrue(implContent.contains("result.setIsActive("),
                "Should set isActive on result");
        assertTrue(implContent.contains("result.setRemark("),
                "Should set remark on result");
        assertTrue(implContent.contains("result.setCreatedAt("),
                "Should set createdAt on result");
        assertTrue(implContent.contains("result.setUpdatedAt("),
                "Should set updatedAt on result");

        // 返回
        assertTrue(implContent.contains("GetItemDetailResp result = new GetItemDetailResp()"),
                "Should create GetItemDetailResp instance");
        assertTrue(implContent.contains("return result;"),
                "Should return the result");

        // ============================================================
        // === 验证没有生成 api-docs 目录 ===
        // ============================================================
        File apiDocsDir = new File(basedir, "api-docs");
        assertFalse(apiDocsDir.exists(), "api-docs directory should NOT be generated");
    }

}

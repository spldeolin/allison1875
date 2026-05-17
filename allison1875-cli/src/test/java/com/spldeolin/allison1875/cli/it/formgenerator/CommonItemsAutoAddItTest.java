package com.spldeolin.allison1875.cli.it.formgenerator;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import org.junit.jupiter.api.Test;

/**
 * common-items-auto-add 集成测试。
 *
 * <p>验证 form-generator 自动添加的公共字段（业务主键 + 审计字段）行为：
 * <ul>
 *   <li>{@code productCode}：自动添加为 items 首位，类型 text，maxLength=36，initPattern=TODO，editPattern=DO_NOT，
 *       在 DDL 中为 {@code VARCHAR(36) NOT NULL}，自动创建唯一索引</li>
 *   <li>{@code createdAt}：自动添加为 items 末尾，initPattern=TODO，editPattern=DO_NOT，
 *       在 DDL 中为 {@code DATETIME NOT NULL}，不在 SaveReq 中</li>
 *   <li>{@code updatedAt}：自动添加为 items 末尾，initPattern=TODO，editPattern=TODO，
 *       在 DDL 中为 {@code DATETIME NOT NULL}，编辑时由 Service 设置</li>
 * </ul>
 *
 * <p>用户 DSL 中只包含 productName（text）和 stock（number）两个字段，
 * 以上三个字段全部由 form-generator 自动注入。
 *
 * @author Deolin 2026-05-17
 */
public class CommonItemsAutoAddItTest extends FormGeneratorItBaseTest {

    @Test
    void test() throws IOException {
        runFormGenerator("common-items-auto-add");

        // ============================================================
        // === DDL 验证 ===
        // ============================================================
        File ddlFile = new File(basedir, "sql/ddl.sql");
        assertTrue(ddlFile.exists(), "DDL file should be generated at sql/ddl.sql");
        String ddl = Files.readString(ddlFile.toPath());
        assertTrue(ddl.contains("CREATE TABLE"), "DDL should contain CREATE TABLE");

        // 业务主键 product_code — VARCHAR(36) NOT NULL
        assertTrue(ddl.contains("`product_code` VARCHAR(36) NOT NULL"),
                "DDL should contain auto-added bizId 'product_code' VARCHAR(36) NOT NULL");

        // 审计字段
        assertTrue(ddl.contains("`created_at` DATETIME NOT NULL"),
                "DDL should contain auto-added 'created_at' DATETIME NOT NULL");
        assertTrue(ddl.contains("`updated_at` DATETIME NOT NULL"),
                "DDL should contain auto-added 'updated_at' DATETIME NOT NULL");

        // 用户字段
        assertTrue(ddl.contains("`product_name` VARCHAR(100) NOT NULL"),
                "DDL should contain user-defined 'product_name'");
        assertTrue(ddl.contains("`stock` BIGINT NOT NULL"),
                "DDL should contain user-defined 'stock' BIGINT NOT NULL");

        // 唯一索引：product_code 应有一个 UNIQUE KEY
        assertTrue(ddl.contains("uk_product_code") || (ddl.contains("UNIQUE") && ddl.contains("product_code")),
                "DDL should contain unique index on product_code");

        // ============================================================
        // === Entity 验证 ===
        // ============================================================
        File entityFile = new File(basedir, "src/main/java/com/example/entity/ProductEntity.java");
        assertTrue(entityFile.exists(), "ProductEntity file should be generated");
        String entityContent = Files.readString(entityFile.toPath());
        assertTrue(entityContent.contains("class ProductEntity"), "Entity should declare class ProductEntity");

        // 自动添加的字段必须存在
        assertTrue(entityContent.contains("productCode"), "Entity should contain productCode (auto bizId)");
        assertTrue(entityContent.contains("createdAt"), "Entity should contain createdAt (auto audit)");
        assertTrue(entityContent.contains("updatedAt"), "Entity should contain updatedAt (auto audit)");

        // 用户定义的字段必须存在
        assertTrue(entityContent.contains("productName"), "Entity should contain productName (user-defined)");
        assertTrue(entityContent.contains("stock"), "Entity should contain stock (user-defined)");

        // ============================================================
        // === SaveReq DTO 验证：自动添加字段不应出现 ===
        // === productCode initPattern=TODO → 不进入 SaveReq
        // === createdAt   initPattern=TODO → 不进入 SaveReq
        // === updatedAt   initPattern=TODO → 不进入 SaveReq
        // ============================================================
        File saveReqFile = new File(basedir, "src/main/java/com/example/dto/req/SaveProductReq.java");
        assertTrue(saveReqFile.exists(), "SaveProductReq DTO should be generated");
        String saveReqContent = Files.readString(saveReqFile.toPath());

        // 用户字段应出现
        assertTrue(saveReqContent.contains("productName"), "SaveReq should contain productName (user-defined)");
        assertTrue(saveReqContent.contains("stock"), "SaveReq should contain stock (user-defined)");

        // productCode 作为业务 ID 出现在 SaveReq 中（编辑时需要用它查已有记录），
        // 但不应有校验注解（@NotBlank/@NotNull）——它不是用户输入
        assertTrue(saveReqContent.contains("productCode"),
                "SaveReq should contain productCode as bizId for edit lookup");
        // productCode 字段不应有 @NotBlank 或 @NotNull
        assertFalse(
                saveReqContent.contains("@NotBlank String productCode")
                        || saveReqContent.contains("@NotNull String productCode"),
                "productCode in SaveReq should NOT have @NotBlank/@NotNull (not user input)");
        // createdAt/updatedAt 不应出现在 SaveReq 中
        assertFalse(saveReqContent.contains("createdAt"),
                "SaveReq should NOT contain createdAt (initPattern=TODO)");
        assertFalse(saveReqContent.contains("updatedAt"),
                "SaveReq should NOT contain updatedAt (initPattern=TODO)");

        // ============================================================
        // === Save ServiceImpl 验证 ===
        // ============================================================
        File saveServiceImplFile = new File(basedir,
                "src/main/java/com/example/service/impl/SaveProductServiceImpl.java");
        assertTrue(saveServiceImplFile.exists(), "SaveProductServiceImpl should be generated");
        String saveContent = Files.readString(saveServiceImplFile.toPath());

        // 创建分支 — productCode 应通过 shortUuid 生成（业务主键自动生成，非 TODO 模式）
        assertTrue(saveContent.contains("product.setProductCode("),
                "Save service toCreate branch should call product.setProductCode()");
        // productCode 使用 shortUuid 生成（UUID.randomUUID()），出现在 toCreate 分支
        assertTrue(saveContent.contains("UUID.randomUUID()"),
                "Save service should use UUID.randomUUID() for shortUuid bizId generation");

        // 验证 setProductCode 在 setCreatedAt 前（创建分支内部）
        int setCodeIndex = saveContent.indexOf("product.setProductCode(");
        int setCreatedAtIndex = saveContent.indexOf("product.setCreatedAt(");
        assertTrue(setCodeIndex < setCreatedAtIndex,
                "setProductCode should appear before setCreatedAt (inside toCreate branch)");

        // createdAt — 创建分支里出现
        assertTrue(saveContent.contains("product.setCreatedAt("),
                "Save service should set createdAt in toCreate branch");
        // updatedAt — common 节出现（无论创建还是编辑，editPattern=TODO）
        assertTrue(saveContent.contains("product.setUpdatedAt("),
                "Save service should set updatedAt in common section");

        // 确保 product.setCreatedAt 只在 toCreate 分支出现（editPattern=DO_NOT）
        int elseIndex = saveContent.indexOf("} else {");
        String elseContent = saveContent.substring(elseIndex);
        assertFalse(elseContent.contains("product.setCreatedAt("),
                "createdAt should NOT be set in else/edit branch (editPattern=DO_NOT)");

        // ============================================================
        // === Mapper 验证：productCode 唯一索引 → queryByProductCode ===
        // ============================================================
        File mapperFile = new File(basedir, "src/main/java/com/example/mapper/ProductMapper.java");
        assertTrue(mapperFile.exists(), "ProductMapper interface should be generated");
        String mapperContent = Files.readString(mapperFile.toPath());
        assertTrue(mapperContent.contains("queryByProductCode"),
                "Mapper should contain queryByProductCode (unique index on bizId)");

        // ============================================================
        // === GetDetailResp 验证：自动字段正常返回 ===
        // ============================================================
        File getDetailRespFile = new File(basedir, "src/main/java/com/example/dto/resp/GetProductDetailResp.java");
        assertTrue(getDetailRespFile.exists(), "GetProductDetailResp should be generated");
        String getDetailRespContent = Files.readString(getDetailRespFile.toPath());
        assertTrue(getDetailRespContent.contains("productCode"),
                "GetDetailResp should contain productCode (bizId)");
        assertTrue(getDetailRespContent.contains("createdAt"),
                "GetDetailResp should contain createdAt (audit)");
        assertTrue(getDetailRespContent.contains("updatedAt"),
                "GetDetailResp should contain updatedAt (audit)");

        // ============================================================
        // === ListResp 中不包含 secret（无 secret 字段，但验证自动字段） ===
        // ============================================================
        File listRespFile = new File(basedir, "src/main/java/com/example/dto/resp/ListProductsResp.java");
        assertTrue(listRespFile.exists(), "ListProductsResp should be generated");
        String listRespContent = Files.readString(listRespFile.toPath());
        // 自动字段正常返回
        assertTrue(listRespContent.contains("productCode"),
                "ListResp should contain productCode (bizId)");
        assertTrue(listRespContent.contains("createdAt"),
                "ListResp should contain createdAt (audit)");
        assertTrue(listRespContent.contains("updatedAt"),
                "ListResp should contain updatedAt (audit)");

        // ============================================================
        // === Mapper XML 验证 ===
        // ============================================================
        File xmlFile = new File(basedir, "src/main/resources/mapper/ProductMapper.xml");
        assertTrue(xmlFile.exists(), "ProductMapper XML should be generated");
        String xmlContent = Files.readString(xmlFile.toPath());
        assertTrue(xmlContent.contains("product_code"), "Mapper XML should reference product_code");
        assertTrue(xmlContent.contains("queryByProductCode"),
                "Mapper XML should contain queryByProductCode statement");

        // ============================================================
        // === 验证没有生成 api-docs 目录 ===
        // ============================================================
        File apiDocsDir = new File(basedir, "api-docs");
        assertFalse(apiDocsDir.exists(), "api-docs directory should NOT be generated when enableDocAnalyzer=false");
    }

}

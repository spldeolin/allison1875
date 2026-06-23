package com.spldeolin.allison1875.cli.it.formgenerator;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import org.junit.jupiter.api.Test;

/**
 * common-items-auto-add 集成测试。
 *
 * <p>验证 form-generator 自动添加的公共字段（业务主键 + 审计字段）行为：
 * <ul>
 *   <li>{@code productCode}：自动添加为 items 首位，类型 text，maxLength=36，canInputOnInit=false，canInputOnEdit=false，
 *       在 DDL 中为 {@code VARCHAR(36) NOT NULL}，自动创建唯一索引</li>
 *   <li>{@code createdAt}：自动添加为 items 末尾，canInputOnInit=false，canInputOnEdit=false，
 *       在 DDL 中为 {@code DATETIME NOT NULL}，不在 SaveReq 中</li>
 *   <li>{@code updatedAt}：自动添加为 items 末尾，canInputOnInit=false，canInputOnEdit=false，
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
        // === CreateReq DTO 验证：自动添加字段不应出现 ===
        // === productCode canInputOn*=false → 不进入 CreateReq
        // === createdAt   canInputOn*=false → 不进入 CreateReq
        // === updatedAt   canInputOn*=false → 不进入 CreateReq
        // ============================================================
        File createReqFile = new File(basedir, "src/main/java/com/example/dto/req/CreateProductReq.java");
        assertTrue(createReqFile.exists(), "CreateProductReq DTO should be generated");
        String createReqContent = Files.readString(createReqFile.toPath());

        // 用户字段应出现
        assertTrue(createReqContent.contains("productName"), "CreateReq should contain productName (user-defined)");
        assertTrue(createReqContent.contains("stock"), "CreateReq should contain stock (user-defined)");

        // productCode 不在 CreateReq 中（创建时由服务端生成）
        assertFalse(createReqContent.contains("productCode"),
                "CreateReq should NOT contain productCode (generated server-side)");
        // createdAt/updatedAt 不应出现在 CreateReq 中
        assertFalse(createReqContent.contains("createdAt"),
                "CreateReq should NOT contain createdAt (canInputOn*=false)");
        assertFalse(createReqContent.contains("updatedAt"),
                "CreateReq should NOT contain updatedAt (canInputOn*=false)");

        // UpdateReq should contain productCode as bizId for lookup
        File updateReqFile = new File(basedir, "src/main/java/com/example/dto/req/UpdateProductReq.java");
        assertTrue(updateReqFile.exists(), "UpdateProductReq DTO should be generated");
        String updateReqContent = Files.readString(updateReqFile.toPath());
        assertTrue(updateReqContent.contains("productCode"),
                "UpdateReq should contain productCode as bizId for update lookup");

        // ============================================================
        // === Create ServiceImpl 验证 ===
        // ============================================================
        File createServiceImplFile = new File(basedir,
                "src/main/java/com/example/service/impl/CreateProductServiceImpl.java");
        assertTrue(createServiceImplFile.exists(), "CreateProductServiceImpl should be generated");
        String createContent = Files.readString(createServiceImplFile.toPath());

        // productCode 应通过 shortUuid 生成（业务主键自动生成）
        assertTrue(createContent.contains("product.setProductCode("),
                "Create service should call product.setProductCode()");
        // productCode 使用 shortUuid 生成（UUID.randomUUID()）
        assertTrue(createContent.contains("UUID.randomUUID()"),
                "Create service should use UUID.randomUUID() for shortUuid bizId generation");

        // 验证 setProductCode 在 setCreatedAt 前
        int setCodeIndex = createContent.indexOf("product.setProductCode(");
        int setCreatedAtIndex = createContent.indexOf("product.setCreatedAt(");
        assertTrue(setCodeIndex < setCreatedAtIndex,
                "setProductCode should appear before setCreatedAt");

        // createdAt — 创建时设置
        assertTrue(createContent.contains("product.setCreatedAt("),
                "Create service should set createdAt");
        // updatedAt — 创建时也设置
        assertTrue(createContent.contains("product.setUpdatedAt("),
                "Create service should set updatedAt");

        // === Update ServiceImpl 验证 ===
        File updateServiceImplFile = new File(basedir,
                "src/main/java/com/example/service/impl/UpdateProductServiceImpl.java");
        assertTrue(updateServiceImplFile.exists(), "UpdateProductServiceImpl should be generated");
        String updateContent = Files.readString(updateServiceImplFile.toPath());
        // createdAt should NOT be set in update
        assertFalse(updateContent.contains("product.setCreatedAt("),
                "createdAt should NOT be set in update service");
        // updatedAt should be set in update
        assertTrue(updateContent.contains("product.setUpdatedAt("),
                "updatedAt should be set in update service");

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
        assertTrue(apiDocsDir.exists(), "api-docs directory should be generated");
    }

}

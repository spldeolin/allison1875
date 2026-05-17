package com.spldeolin.allison1875.cli.it.formgenerator;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import org.junit.jupiter.api.Test;

/**
 * select-item 集成测试。
 *
 * <p>验证 select 类型字段的枚举生成和引用：
 * <ul>
 *   <li>生成 {@code XxxEnum.java} 枚举文件，包含 {@code code}、{@code title} 字段和 {@code of()}、{@code valid()} 方法</li>
 *   <li>DTO 中字段类型引用生成的枚举类型（{@code ProductStatusEnum}）</li>
 *   <li>DDL 中列类型为 {@code VARCHAR(64)}</li>
 *   <li>Save API 的 Service 逻辑中包含 {@code .getCode()} 转换（枚举 → String）</li>
 *   <li>GetDetail API 的 Service 逻辑中包含 {@code XxxEnum.of()} 转换（String → 枚举）</li>
 *   <li>ListReq 中生成 {@code List<EnumType>} 列表过滤字段</li>
 * </ul>
 *
 * @author Deolin 2026-05-17
 */
public class SelectItemItTest extends FormGeneratorItBaseTest {

    @Test
    void test() throws IOException {
        runFormGenerator("select-item");

        // === DDL 验证 ===
        File ddlFile = new File(basedir, "sql/ddl.sql");
        assertTrue(ddlFile.exists(), "DDL file should be generated at sql/ddl.sql");
        String ddl = new String(Files.readAllBytes(ddlFile.toPath()), StandardCharsets.UTF_8);
        assertTrue(ddl.contains("CREATE TABLE"), "DDL should contain CREATE TABLE");
        // select 字段在 DDL 中为 VARCHAR(64)，isNonVoid=true → NOT NULL
        assertTrue(ddl.contains("`product_status` VARCHAR(64) NOT NULL"),
                "productStatus (select+nonVoid) should be VARCHAR(64) NOT NULL");
        assertTrue(ddl.contains("`product_category` VARCHAR(64)"),
                "productCategory (select+nullable) should be VARCHAR(64) without NOT NULL");
        assertFalse(ddl.contains("`product_category` VARCHAR(64) NOT NULL"),
                "productCategory should NOT have NOT NULL since isNonVoid=false");
        // 审计字段
        assertTrue(ddl.contains("`product_code`"), "DDL should contain auto-added bizId column 'product_code'");

        // === Enum 验证：ProductStatusEnum ===
        File statusEnumFile = new File(basedir, "src/main/java/com/example/enums/ProductStatusEnum.java");
        assertTrue(statusEnumFile.exists(), "ProductStatusEnum file should be generated");
        String statusEnumContent = new String(Files.readAllBytes(statusEnumFile.toPath()), StandardCharsets.UTF_8);
        assertTrue(statusEnumContent.contains("enum ProductStatusEnum"), "Should declare enum ProductStatusEnum");
        assertTrue(statusEnumContent.contains("@Getter"), "Enum should have @Getter");
        assertTrue(statusEnumContent.contains("@AllArgsConstructor"), "Enum should have @AllArgsConstructor");
        assertTrue(statusEnumContent.contains("DRAFT"), "Enum should contain DRAFT entry");
        assertTrue(statusEnumContent.contains("PUBLISHED"), "Enum should contain PUBLISHED entry");
        assertTrue(statusEnumContent.contains("ARCHIVED"), "Enum should contain ARCHIVED entry");
        // code 和 title 字段
        assertTrue(statusEnumContent.contains("@JsonValue"), "Enum should have @JsonValue on code field");
        assertTrue(statusEnumContent.contains("private final String code;"),
                "Enum should have private final String code field");
        assertTrue(statusEnumContent.contains("private final String title;"),
                "Enum should have private final String title field");
        // valid() 方法
        assertTrue(statusEnumContent.contains("public static boolean valid"), "Enum should have valid() method");
        // of() 方法
        assertTrue(statusEnumContent.contains("public static ProductStatusEnum of"),
                "Enum should have of() method");
        assertTrue(statusEnumContent.contains("@JsonCreator"), "of() method should have @JsonCreator");

        // === Enum 验证：ProductCategoryEnum ===
        File categoryEnumFile = new File(basedir, "src/main/java/com/example/enums/ProductCategoryEnum.java");
        assertTrue(categoryEnumFile.exists(), "ProductCategoryEnum file should be generated");
        String categoryEnumContent = new String(Files.readAllBytes(categoryEnumFile.toPath()), StandardCharsets.UTF_8);
        assertTrue(categoryEnumContent.contains("enum ProductCategoryEnum"),
                "Should declare enum ProductCategoryEnum");
        assertTrue(categoryEnumContent.contains("ELECTRONICS"), "Enum should contain ELECTRONICS entry");
        assertTrue(categoryEnumContent.contains("CLOTHING"), "Enum should contain CLOTHING entry");
        assertTrue(categoryEnumContent.contains("public static ProductCategoryEnum of"),
                "Enum should have of() method");

        // === Entity 验证 ===
        File entityFile = new File(basedir, "src/main/java/com/example/entity/ProductEntity.java");
        assertTrue(entityFile.exists(), "Entity file should be generated");
        String entityContent = Files.readString(entityFile.toPath());
        assertTrue(entityContent.contains("class ProductEntity"), "Entity should declare class ProductEntity");
        // Entity 中 select 字段存储为 String（枚举的 code 值）
        assertTrue(entityContent.contains("String productStatus"),
                "Entity should contain String productStatus field");
        assertTrue(entityContent.contains("String productCategory"),
                "Entity should contain String productCategory field");

        // === SaveReq DTO 验证 ===
        File saveReqFile = new File(basedir, "src/main/java/com/example/dto/req/SaveProductReq.java");
        assertTrue(saveReqFile.exists(), "SaveProductReq DTO should be generated");
        String saveReqContent = new String(Files.readAllBytes(saveReqFile.toPath()), StandardCharsets.UTF_8);
        assertTrue(saveReqContent.contains("class SaveProductReq"), "Should contain class SaveProductReq");
        // select 字段引用枚举类型
        assertTrue(saveReqContent.contains("ProductStatusEnum productStatus"),
                "SaveReq should contain ProductStatusEnum productStatus");
        assertTrue(saveReqContent.contains("ProductCategoryEnum productCategory"),
                "SaveReq should contain ProductCategoryEnum productCategory");
        // isNonVoid=true 附加 @NotNull
        assertTrue(
                saveReqContent.contains("@NotNull") || saveReqContent.contains("@javax.validation.constraints.NotNull"),
                "nonVoid select field should have @NotNull");

        // === Save ServiceImpl 验证：枚举 .getCode() 转换 ===
        File saveServiceImplFile = new File(basedir, "src/main/java/com/example/service/impl/SaveProductServiceImpl.java");
        assertTrue(saveServiceImplFile.exists(), "SaveProductServiceImpl file should be generated");
        String saveServiceImplContent = new String(Files.readAllBytes(saveServiceImplFile.toPath()),
                StandardCharsets.UTF_8);
        // isNonVoid=true → 直接 .getCode()
        assertTrue(saveServiceImplContent.contains("req.getProductStatus().getCode()"),
                "Save service should call req.getProductStatus().getCode() for nonVoid select");
        // isNonVoid=false → null 安全转换（生成代码格式为 " != null ? ... : null"）
        assertTrue(saveServiceImplContent.contains("req.getProductCategory() != null"),
                "Save service should null-check productCategory before .getCode()");
        assertTrue(saveServiceImplContent.contains("req.getProductCategory().getCode()"),
                "Save service should call req.getProductCategory().getCode() when non-null");

        // === GetDetailResp DTO 验证 ===
        File getDetailRespFile = new File(basedir, "src/main/java/com/example/dto/resp/GetProductDetailResp.java");
        assertTrue(getDetailRespFile.exists(), "GetProductDetailResp DTO should be generated");
        String getDetailRespContent = new String(Files.readAllBytes(getDetailRespFile.toPath()),
                StandardCharsets.UTF_8);
        assertTrue(getDetailRespContent.contains("class GetProductDetailResp"),
                "Should contain class GetProductDetailResp");
        assertTrue(getDetailRespContent.contains("ProductStatusEnum productStatus"),
                "GetDetailResp should contain ProductStatusEnum productStatus");
        assertTrue(getDetailRespContent.contains("ProductCategoryEnum productCategory"),
                "GetDetailResp should contain ProductCategoryEnum productCategory");

        // === GetDetail ServiceImpl 验证：Enum.of() 转换 ===
        File getDetailServiceImplFile = new File(basedir,
                "src/main/java/com/example/service/impl/GetProductDetailServiceImpl.java");
        assertTrue(getDetailServiceImplFile.exists(), "GetProductDetailServiceImpl file should be generated");
        String getDetailServiceImplContent = new String(Files.readAllBytes(getDetailServiceImplFile.toPath()),
                StandardCharsets.UTF_8);
        // isNonVoid=true → ProductStatusEnum.of()
        assertTrue(getDetailServiceImplContent.contains("ProductStatusEnum.of(product.getProductStatus())"),
                "GetDetail service should call ProductStatusEnum.of() for nonVoid select");
        // isNonVoid=false → ProductCategoryEnum.of()
        assertTrue(getDetailServiceImplContent.contains("ProductCategoryEnum.of"),
                "GetDetail service should call ProductCategoryEnum.of() for nullable select");

        // === ListReq DTO 验证 ===
        File listReqFile = new File(basedir, "src/main/java/com/example/dto/req/ListProductsReq.java");
        assertTrue(listReqFile.exists(), "ListProductsReq DTO should be generated");
        String listReqContent = new String(Files.readAllBytes(listReqFile.toPath()), StandardCharsets.UTF_8);
        assertTrue(listReqContent.contains("class ListProductsReq"), "Should contain class ListProductsReq");
        // select 字段在 ListReq 中为 List<EnumType> 列表过滤
        assertTrue(listReqContent.contains("List<ProductStatusEnum> productStatus"),
                "ListReq should contain List<ProductStatusEnum> for in-filter");
        assertTrue(listReqContent.contains("List<ProductCategoryEnum> productCategory"),
                "ListReq should contain List<ProductCategoryEnum> for in-filter");

        // === List ServiceImpl 验证：query-transformer 转换 Design Chain ===
        File listServiceImplFile = new File(basedir,
                "src/main/java/com/example/service/impl/ListProductsServiceImpl.java");
        assertTrue(listServiceImplFile.exists(), "ListProductsServiceImpl file should be generated");
        String listServiceImplContent = new String(Files.readAllBytes(listServiceImplFile.toPath()),
                StandardCharsets.UTF_8);
        assertTrue(listServiceImplContent.contains("productMapper.countProduct("),
                "List service should call productMapper.countProduct");
        assertTrue(listServiceImplContent.contains("productMapper.queryProduct("),
                "List service should call productMapper.queryProduct");

        // === 验证没有生成 api-docs 目录（enableDocAnalyzer=false） ===
        File apiDocsDir = new File(basedir, "api-docs");
        assertFalse(apiDocsDir.exists(), "api-docs directory should NOT be generated when enableDocAnalyzer=false");
    }

}

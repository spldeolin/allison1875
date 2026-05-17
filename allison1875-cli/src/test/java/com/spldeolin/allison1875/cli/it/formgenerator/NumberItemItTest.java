package com.spldeolin.allison1875.cli.it.formgenerator;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import org.junit.jupiter.api.Test;

/**
 * number-item 集成测试。
 *
 * <p>验证 number 类型字段的全路径处理：
 * <ul>
 *   <li>DDL 中 {@code canBeDecimal=true} → {@code DECIMAL(14, 4)}，{@code canBeDecimal=false} → {@code BIGINT}</li>
 *   <li>DTO 中 {@code canBeDecimal=true} → {@code BigDecimal}，{@code canBeDecimal=false} → {@code Long}</li>
 *   <li>校验注解：{@code isNonVoid=true} → {@code @NotNull}</li>
 *   <li>ListReq 中为 {@code List<BigDecimal>} 或 {@code List<Long>} 列表过滤（{@code .in()}）</li>
 *   <li>GetDetailResp 中该字段正常返回</li>
 * </ul>
 *
 * @author Deolin 2026-05-17
 */
public class NumberItemItTest extends FormGeneratorItBaseTest {

    @Test
    void test() throws IOException {
        runFormGenerator("number-item");

        // === DDL 验证 ===
        File ddlFile = new File(basedir, "sql/ddl.sql");
        assertTrue(ddlFile.exists(), "DDL file should be generated at sql/ddl.sql");
        String ddl = new String(Files.readAllBytes(ddlFile.toPath()), StandardCharsets.UTF_8);
        assertTrue(ddl.contains("CREATE TABLE"), "DDL should contain CREATE TABLE");
        // price: canBeDecimal=true, isNonVoid=true → DECIMAL(14,4) NOT NULL
        assertTrue(ddl.contains("`price` DECIMAL(14, 4) NOT NULL COMMENT '价格'"),
                "price (decimal+nonVoid) should be DECIMAL(14,4) NOT NULL");
        // stock: canBeDecimal=false, isNonVoid=true → BIGINT NOT NULL
        assertTrue(ddl.contains("`stock` BIGINT NOT NULL COMMENT '库存'"),
                "stock (integer+nonVoid) should be BIGINT NOT NULL");
        // discount: canBeDecimal=true, isNonVoid=false → DECIMAL(14,4) (no NOT NULL)
        assertTrue(ddl.contains("`discount` DECIMAL(14, 4)"),
                "discount (decimal+nullable) should be DECIMAL(14,4)");
        assertFalse(ddl.contains("`discount` DECIMAL(14, 4) NOT NULL"),
                "discount should NOT have NOT NULL since isNonVoid=false");
        // sortOrder: canBeDecimal=false, isNonVoid=false → BIGINT (no NOT NULL)
        assertTrue(ddl.contains("`sort_order` BIGINT"),
                "sortOrder (integer+nullable) should be BIGINT");
        assertFalse(ddl.contains("`sort_order` BIGINT NOT NULL"),
                "sortOrder should NOT have NOT NULL since isNonVoid=false");

        // === Entity 验证 ===
        File entityFile = new File(basedir, "src/main/java/com/example/entity/ProductEntity.java");
        assertTrue(entityFile.exists(), "Entity file should be generated");
        String entityContent = new String(Files.readAllBytes(entityFile.toPath()), StandardCharsets.UTF_8);
        assertTrue(entityContent.contains("class ProductEntity"), "Entity should declare class ProductEntity");
        // price → BigDecimal
        assertTrue(entityContent.contains("BigDecimal price"), "Entity should contain BigDecimal price field");
        // stock → Long
        assertTrue(entityContent.contains("Long stock"), "Entity should contain Long stock field");
        // discount → BigDecimal
        assertTrue(entityContent.contains("BigDecimal discount"), "Entity should contain BigDecimal discount field");
        // sortOrder → Long
        assertTrue(entityContent.contains("Long sortOrder"), "Entity should contain Long sortOrder field");

        // === SaveReq DTO 验证 ===
        File saveReqFile = new File(basedir, "src/main/java/com/example/dto/req/SaveProductReq.java");
        assertTrue(saveReqFile.exists(), "SaveProductReq DTO should be generated");
        String saveReqContent = new String(Files.readAllBytes(saveReqFile.toPath()), StandardCharsets.UTF_8);
        assertTrue(saveReqContent.contains("class SaveProductReq"), "Should contain class SaveProductReq");
        // price: BigDecimal, isNonVoid=true → @NotNull
        assertTrue(saveReqContent.contains("BigDecimal price"),
                "SaveReq should contain BigDecimal price field");
        assertTrue(saveReqContent.contains("@NotNull") || saveReqContent.contains("@javax.validation.constraints.NotNull"),
                "price with isNonVoid=true should have @NotNull");
        // stock: Long, isNonVoid=true → @NotNull
        assertTrue(saveReqContent.contains("Long stock"), "SaveReq should contain Long stock field");
        // discount: BigDecimal, isNonVoid=false → no @NotNull on field itself (but check field exists)
        assertTrue(saveReqContent.contains("BigDecimal discount"),
                "SaveReq should contain BigDecimal discount field");
        // sortOrder: Long, isNonVoid=false
        assertTrue(saveReqContent.contains("Long sortOrder"),
                "SaveReq should contain Long sortOrder field");

        // === ListReq DTO 验证 ===
        File listReqFile = new File(basedir, "src/main/java/com/example/dto/req/ListProductsReq.java");
        assertTrue(listReqFile.exists(), "ListProductsReq DTO should be generated");
        String listReqContent = new String(Files.readAllBytes(listReqFile.toPath()), StandardCharsets.UTF_8);
        assertTrue(listReqContent.contains("class ListProductsReq"), "Should contain class ListProductsReq");
        // number 字段在 ListReq 中为 List<BigDecimal> 或 List<Long>，用于 in 过滤
        assertTrue(listReqContent.contains("List<BigDecimal> price"),
                "ListReq should contain List<BigDecimal> price for IN filter");
        assertTrue(listReqContent.contains("List<Long> stock"),
                "ListReq should contain List<Long> stock for IN filter");
        assertTrue(listReqContent.contains("List<BigDecimal> discount"),
                "ListReq should contain List<BigDecimal> discount for IN filter");
        assertTrue(listReqContent.contains("List<Long> sortOrder"),
                "ListReq should contain List<Long> sortOrder for IN filter");

        // === GetDetailResp DTO 验证 ===
        File getDetailRespFile = new File(basedir, "src/main/java/com/example/dto/resp/GetProductDetailResp.java");
        assertTrue(getDetailRespFile.exists(), "GetProductDetailResp DTO should be generated");
        String getDetailRespContent = new String(Files.readAllBytes(getDetailRespFile.toPath()),
                StandardCharsets.UTF_8);
        assertTrue(getDetailRespContent.contains("class GetProductDetailResp"),
                "Should contain class GetProductDetailResp");
        assertTrue(getDetailRespContent.contains("BigDecimal price"),
                "GetDetailResp should contain BigDecimal price");
        assertTrue(getDetailRespContent.contains("Long stock"), "GetDetailResp should contain Long stock");
        assertTrue(getDetailRespContent.contains("BigDecimal discount"),
                "GetDetailResp should contain BigDecimal discount");
        assertTrue(getDetailRespContent.contains("Long sortOrder"), "GetDetailResp should contain Long sortOrder");

        // === List ServiceImpl 验证：query-transformer 将 Design Chain 转为 Mapper 调用 ===
        File listServiceImplFile = new File(basedir,
                "src/main/java/com/example/service/impl/ListProductsServiceImpl.java");
        assertTrue(listServiceImplFile.exists(), "ListProductsServiceImpl file should be generated");
        String listServiceImplContent = new String(Files.readAllBytes(listServiceImplFile.toPath()),
                StandardCharsets.UTF_8);
        assertTrue(listServiceImplContent.contains("productMapper.countProduct("),
                "List service should call productMapper.countProduct for pagination");
        assertTrue(listServiceImplContent.contains("productMapper.queryProduct("),
                "List service should call productMapper.queryProduct for query");
        // Param 包含 number 字段
        assertTrue(listServiceImplContent.contains("setPrice("),
                "Param should set price from req (number field)");
        assertTrue(listServiceImplContent.contains("setStock("),
                "Param should set stock from req (number field)");
        assertTrue(listServiceImplContent.contains("setDiscount("),
                "Param should set discount from req (number field)");
        assertTrue(listServiceImplContent.contains("setSortOrder("),
                "Param should set sortOrder from req (number field)");

        // === 验证没有生成 api-docs 目录（enableDocAnalyzer=false） ===
        File apiDocsDir = new File(basedir, "api-docs");
        assertFalse(apiDocsDir.exists(), "api-docs directory should NOT be generated when enableDocAnalyzer=false");
    }

}

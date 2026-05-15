package com.spldeolin.allison1875.cli.it.persistencegenerator;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import org.junit.jupiter.api.Test;

/**
 * persistence-generator composite-index 集成测试。
 *
 * @author Deolin 2026-05-14
 */
public class CompositeIndexItTest extends PersistenceGeneratorItBaseTest {

    @Test
    void test() throws IOException {
        runPersistenceGenerator("composite-index");

        File mapperFile = new File(basedir, "src/main/java/com/example/mapper/TInventoryMapper.java");
        assertTrue(mapperFile.exists(), "TInventoryMapper.java should be generated");
        String mapperContent = new String(Files.readAllBytes(mapperFile.toPath()), StandardCharsets.UTF_8);

        assertTrue(mapperContent.contains("queryByWarehouseId"), "Should have queryByWarehouseId");
        assertTrue(mapperContent.contains("queryByWarehouseIdProductId"), "Should have queryByWarehouseIdProductId");
        assertTrue(mapperContent.contains("queryByWarehouseIdProductIdSkuCode"),
                "Should have queryByWarehouseIdProductIdSkuCode");
        assertTrue(mapperContent.contains("queryByProductId"), "Should have queryByProductId");
        assertTrue(mapperContent.contains("queryByProductIdSkuCode"), "Should have queryByProductIdSkuCode");
        assertTrue(mapperContent.contains("List"), "Non-unique index queries should return List");
        assertTrue(mapperContent.contains("deleteByWarehouseId"), "Should have deleteByWarehouseId");
        assertTrue(mapperContent.contains("deleteByProductId"), "Should have deleteByProductId");
        assertFalse(mapperContent.contains("listAll"), "Should NOT have listAll (table has indexes)");

        File xmlFile = new File(basedir, "src/main/resources/mapper/TInventoryMapper.xml");
        assertTrue(xmlFile.exists(), "TInventoryMapper.xml should be generated");
        String xmlContent = new String(Files.readAllBytes(xmlFile.toPath()), StandardCharsets.UTF_8);
        assertTrue(xmlContent.contains("queryByWarehouseIdProductIdSkuCode"), "XML should contain the composite query");
        assertTrue(xmlContent.contains("warehouse_id = #{warehouseId}"), "XML should have warehouse_id condition");
        assertTrue(xmlContent.contains("product_id = #{productId}"), "XML should have product_id condition");
        assertTrue(xmlContent.contains("sku_code = #{skuCode}"), "XML should have sku_code condition");
    }

}

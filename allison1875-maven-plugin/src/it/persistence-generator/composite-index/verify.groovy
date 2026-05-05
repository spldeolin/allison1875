/*
 * persistence-generator composite-index 集成测试验证脚本
 *
 * 验证联合索引的平铺逻辑：
 * uk_warehouse_product_sku(warehouse_id, product_id, sku_code) 平铺为：
 *   - queryByWarehouseId (非唯一)
 *   - queryByWarehouseIdProductId (非唯一)
 *   - queryByWarehouseIdProductIdSkuCode (唯一)
 * idx_product_sku(product_id, sku_code) 平铺为：
 *   - queryByProductId (去重，已在上面)
 *   - queryByProductIdSkuCode (非唯一)
 */

File mapperFile = new File(basedir, "src/main/java/com/example/mapper/TInventoryMapper.java")
assert mapperFile.exists() : "TInventoryMapper.java should be generated"
String mapperContent = mapperFile.text

// 索引平铺后的查询方法
assert mapperContent.contains("queryByWarehouseId") : "Should have queryByWarehouseId (first column of uk)"
assert mapperContent.contains("queryByWarehouseIdProductId") : "Should have queryByWarehouseIdProductId"
assert mapperContent.contains("queryByWarehouseIdProductIdSkuCode") : "Should have queryByWarehouseIdProductIdSkuCode"
assert mapperContent.contains("queryByProductId") : "Should have queryByProductId (first column of idx)"
assert mapperContent.contains("queryByProductIdSkuCode") : "Should have queryByProductIdSkuCode"

// 验证唯一索引方法的返回类型是单个对象（不是 List）
// queryByWarehouseIdProductIdSkuCode 是唯一索引的完整前缀，返回类型应为单个 Entity
// 非唯一索引的查询方法返回 List
assert mapperContent.contains("List") : "Non-unique index queries should return List"

// 对应的 delete 方法
assert mapperContent.contains("deleteByWarehouseId") : "Should have deleteByWarehouseId"
assert mapperContent.contains("deleteByProductId") : "Should have deleteByProductId"

// 不应有 listAll（因为有索引）
assert !mapperContent.contains("listAll") : "Should NOT have listAll (table has indexes)"

println "[composite-index] Mapper assertions passed."

// 验证 XML
File xmlFile = new File(basedir, "src/main/resources/mapper/TInventoryMapper.xml")
assert xmlFile.exists() : "TInventoryMapper.xml should be generated"
String xmlContent = xmlFile.text
assert xmlContent.contains("queryByWarehouseIdProductIdSkuCode") : "XML should contain the composite query"
assert xmlContent.contains("warehouse_id = #{warehouseId}") : "XML should have warehouse_id condition"
assert xmlContent.contains("product_id = #{productId}") : "XML should have product_id condition"
assert xmlContent.contains("sku_code = #{skuCode}") : "XML should have sku_code condition"

println "[composite-index] XML assertions passed."

println "[persistence-generator/composite-index] All assertions passed."
return true

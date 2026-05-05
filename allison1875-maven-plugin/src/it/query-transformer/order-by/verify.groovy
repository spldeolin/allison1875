/*
 * query-transformer order-by 集成测试验证脚本
 *
 * 验证 ORDER BY 子句的 SQL 生成。
 */

// ========== 1. Mapper XML 验证 ==========
File mapperXmlFile = new File(basedir, "src/main/resources/mapper/TOrderMapper.xml")
assert mapperXmlFile.exists() : "TOrderMapper.xml should exist"
String xmlContent = mapperXmlFile.text

// --- 单字段 ASC ---
assert xmlContent.contains("id='listOrderByCreatedAtAsc'") : "XML should contain listOrderByCreatedAtAsc"
assert xmlContent.contains("ORDER BY") : "XML should contain ORDER BY clause"
assert xmlContent.contains("created_at") : "ORDER BY should reference created_at column"

// --- 单字段 DESC ---
assert xmlContent.contains("id='listOrderByAmountDesc'") : "XML should contain listOrderByAmountDesc"
assert xmlContent.contains("amount DESC") : "ORDER BY should contain 'amount DESC'"

// --- 多字段排序 ---
assert xmlContent.contains("id='listOrderByMultiFields'") : "XML should contain listOrderByMultiFields"

// --- WHERE + ORDER BY 组合 ---
assert xmlContent.contains("id='listByUserIdOrderByCreatedAt'") : "XML should contain listByUserIdOrderByCreatedAt"
// 确保同时有 WHERE 和 ORDER BY
String whereOrderSection = xmlContent.substring(xmlContent.indexOf("id='listByUserIdOrderByCreatedAt'"))
assert whereOrderSection.contains("user_id") : "Should have WHERE user_id condition"
assert whereOrderSection.contains("ORDER BY") : "Should have ORDER BY after WHERE"
assert whereOrderSection.contains("DESC") : "Should have DESC in ORDER BY"

println "[order-by] Mapper XML assertions passed."

// ========== 2. Mapper 接口验证 ==========
File mapperFile = new File(basedir, "src/main/java/com/example/mapper/TOrderMapper.java")
assert mapperFile.exists() : "TOrderMapper.java should exist"
String mapperContent = mapperFile.text

assert mapperContent.contains("listOrderByCreatedAtAsc") : "Mapper should contain listOrderByCreatedAtAsc"
assert mapperContent.contains("listOrderByAmountDesc") : "Mapper should contain listOrderByAmountDesc"
assert mapperContent.contains("listOrderByMultiFields") : "Mapper should contain listOrderByMultiFields"
assert mapperContent.contains("listByUserIdOrderByCreatedAt") : "Mapper should contain listByUserIdOrderByCreatedAt"

println "[order-by] Mapper interface assertions passed."

// ========== 3. Service 文件验证 ==========
File serviceFile = new File(basedir, "src/main/java/com/example/service/OrderService.java")
assert serviceFile.exists() : "OrderService.java should exist"
String serviceContent = serviceFile.text

assert !serviceContent.contains("TOrderDesign.") : "Design chain should be replaced"
assert serviceContent.contains("tOrderMapper.listOrderByCreatedAtAsc") : "Should call mapper method"
assert serviceContent.contains("tOrderMapper.listOrderByAmountDesc") : "Should call mapper method"
assert serviceContent.contains("tOrderMapper.listOrderByMultiFields") : "Should call mapper method"
assert serviceContent.contains("tOrderMapper.listByUserIdOrderByCreatedAt") : "Should call mapper method"

println "[order-by] Service file assertions passed."
println "[query-transformer/order-by] All assertions passed."
return true

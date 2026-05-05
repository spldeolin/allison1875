/*
 * query-transformer update-delete 集成测试验证脚本
 *
 * 验证 update() 和 delete() 链的 SQL 生成。
 */

// ========== 1. Mapper XML 验证 ==========
File mapperXmlFile = new File(basedir, "src/main/resources/mapper/TOrderMapper.xml")
assert mapperXmlFile.exists() : "TOrderMapper.xml should exist"
String xmlContent = mapperXmlFile.text

// --- UPDATE: updateStatusById ---
assert xmlContent.contains("id='updateStatusById'") : "XML should contain updateStatusById"
assert xmlContent.contains("<update") : "XML should contain <update> tag"
assert xmlContent.contains("UPDATE t_order") : "UPDATE should reference t_order"
assert xmlContent.contains("status = #{status}") : "SET should contain status = #{status}"

// --- UPDATE: updateAmountAndRemarkById ---
assert xmlContent.contains("id='updateAmountAndRemarkById'") : "XML should contain updateAmountAndRemarkById"
assert xmlContent.contains("amount = #{amount}") : "SET should contain amount = #{amount}"
assert xmlContent.contains("remark = #{remark}") : "SET should contain remark = #{remark}"

// --- DELETE: deleteOrderById ---
assert xmlContent.contains("id='deleteOrderById'") : "XML should contain deleteOrderById"
assert xmlContent.contains("<delete") : "XML should contain <delete> tag"
assert xmlContent.contains("DELETE FROM t_order") : "DELETE should reference t_order"

// --- DELETE: deleteByUserId ---
assert xmlContent.contains("id='deleteByUserId'") ||
    xmlContent.contains("id='deleteByUserId") : "XML should contain deleteByUserId"

println "[update-delete] Mapper XML assertions passed."

// ========== 2. Mapper 接口验证 ==========
File mapperFile = new File(basedir, "src/main/java/com/example/mapper/TOrderMapper.java")
assert mapperFile.exists() : "TOrderMapper.java should exist"
String mapperContent = mapperFile.text

assert mapperContent.contains("updateStatusById") : "Mapper should contain updateStatusById method"
assert mapperContent.contains("updateAmountAndRemarkById") : "Mapper should contain updateAmountAndRemarkById method"
assert mapperContent.contains("deleteOrderById") : "Mapper should contain deleteOrderById method"
assert mapperContent.contains("deleteByUserId") : "Mapper should contain deleteByUserId method"

// 返回类型应该是 int
assert mapperContent.contains("int updateStatusById") : "UPDATE method should return int"
assert mapperContent.contains("int deleteOrderById") : "DELETE method should return int"

println "[update-delete] Mapper interface assertions passed."

// ========== 3. Service 文件验证 ==========
File serviceFile = new File(basedir, "src/main/java/com/example/service/OrderService.java")
assert serviceFile.exists() : "OrderService.java should exist"
String serviceContent = serviceFile.text

// Design 链已被替换
assert !serviceContent.contains("TOrderDesign.") : "Design chain should be replaced"

// Mapper 被注入
assert serviceContent.contains("TOrderMapper") : "Mapper should be injected"
assert serviceContent.contains("tOrderMapper") : "Mapper field should exist"

// 方法调用
assert serviceContent.contains("tOrderMapper.updateStatusById") : "Should call mapper.updateStatusById"
assert serviceContent.contains("tOrderMapper.updateAmountAndRemarkById") : "Should call mapper.updateAmountAndRemarkById"
assert serviceContent.contains("tOrderMapper.deleteOrderById") : "Should call mapper.deleteOrderById"
assert serviceContent.contains("tOrderMapper.deleteByUserId") : "Should call mapper.deleteByUserId"

println "[update-delete] Service file assertions passed."
println "[query-transformer/update-delete] All assertions passed."
return true

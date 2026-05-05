/*
 * query-transformer where-forced 集成测试验证脚本
 *
 * 验证 .whereEvenNull() 模式下条件不被 <if test> 包裹，直接输出 SQL。
 */

// ========== 1. Mapper XML 验证 ==========
File mapperXmlFile = new File(basedir, "src/main/resources/mapper/TOrderMapper.xml")
assert mapperXmlFile.exists() : "TOrderMapper.xml should exist"
String xmlContent = mapperXmlFile.text

// --- findByIdForced: 强制 eq 条件 ---
assert xmlContent.contains("id='findByIdForced'") : "XML should contain findByIdForced"
// 截取 findByIdForced 相关片段进行验证
String findByIdForcedSection = xmlContent.substring(xmlContent.indexOf("id='findByIdForced'"))
findByIdForcedSection = findByIdForcedSection.substring(0, findByIdForcedSection.indexOf("</select>"))
// 强制模式下不应有 <if test> 包裹
assert !findByIdForcedSection.contains("<if test") : "Forced mode should NOT have <if test> wrapping"
// 应该直接有 AND id = #{id}
assert findByIdForcedSection.contains("AND id = #{id}") : "Forced mode should have direct 'AND id = #{id}'"

// --- listByStatusAndUserIdForced: 强制多条件 ---
assert xmlContent.contains("id='listByStatusAndUserIdForced'") : "XML should contain listByStatusAndUserIdForced"
String listForcedSection = xmlContent.substring(xmlContent.indexOf("id='listByStatusAndUserIdForced'"))
listForcedSection = listForcedSection.substring(0, listForcedSection.indexOf("</select>"))
assert !listForcedSection.contains("<if test") : "Forced multi-condition should NOT have <if test>"
assert listForcedSection.contains("AND status = #{status}") : "Should have direct 'AND status = #{status}'"
assert listForcedSection.contains("AND user_id = #{userId}") : "Should have direct 'AND user_id = #{userId}'"

// --- deleteByIdForced: 强制 DELETE ---
assert xmlContent.contains("id='deleteByIdForced'") : "XML should contain deleteByIdForced"
String deleteForcedSection = xmlContent.substring(xmlContent.indexOf("id='deleteByIdForced'"))
deleteForcedSection = deleteForcedSection.substring(0, deleteForcedSection.indexOf("</delete>"))
assert !deleteForcedSection.contains("<if test") : "Forced DELETE should NOT have <if test>"
assert deleteForcedSection.contains("AND id = #{id}") : "Forced DELETE should have direct 'AND id = #{id}'"

println "[where-forced] Mapper XML assertions passed."

// ========== 2. Mapper 接口验证 ==========
File mapperFile = new File(basedir, "src/main/java/com/example/mapper/TOrderMapper.java")
assert mapperFile.exists() : "TOrderMapper.java should exist"
String mapperContent = mapperFile.text

assert mapperContent.contains("findByIdForced") : "Mapper should contain findByIdForced"
assert mapperContent.contains("listByStatusAndUserIdForced") : "Mapper should contain listByStatusAndUserIdForced"
assert mapperContent.contains("deleteByIdForced") : "Mapper should contain deleteByIdForced"

println "[where-forced] Mapper interface assertions passed."

// ========== 3. Service 文件验证 ==========
File serviceFile = new File(basedir, "src/main/java/com/example/service/OrderService.java")
assert serviceFile.exists() : "OrderService.java should exist"
String serviceContent = serviceFile.text

assert !serviceContent.contains("TOrderDesign.") : "Design chain should be replaced"
assert serviceContent.contains("tOrderMapper.findByIdForced") : "Should call mapper.findByIdForced"
assert serviceContent.contains("tOrderMapper.listByStatusAndUserIdForced") : "Should call mapper.listByStatusAndUserIdForced"
assert serviceContent.contains("tOrderMapper.deleteByIdForced") : "Should call mapper.deleteByIdForced"

println "[where-forced] Service file assertions passed."
println "[query-transformer/where-forced] All assertions passed."
return true

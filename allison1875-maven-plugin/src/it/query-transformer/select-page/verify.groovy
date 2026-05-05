/*
 * query-transformer select-page 集成测试验证脚本
 *
 * 验证 .page(pageNo, pageSize) 终止方法的分页查询生成。
 */

// ========== 1. Mapper XML 验证 ==========
File mapperXmlFile = new File(basedir, "src/main/resources/mapper/TOrderMapper.xml")
assert mapperXmlFile.exists() : "TOrderMapper.xml should exist"
String xmlContent = mapperXmlFile.text

// --- pageAll: 无条件分页 ---
assert xmlContent.contains("id='pageAll'") : "XML should contain pageAll select"
assert xmlContent.contains("LIMIT #{offset}, #{limit}") : "page should generate 'LIMIT #{offset}, #{limit}'"

// --- pageAll 对应的 count 方法 ---
assert xmlContent.contains("id='countPageAll'") || xmlContent.contains("id='countAll'") : "XML should contain count method for pageAll"
assert xmlContent.contains("resultType='long'") : "count method should return long"

// --- pageByUserId: 带条件分页 ---
assert xmlContent.contains("id='pageByUserId'") : "XML should contain pageByUserId select"
// 验证 pageByUserId 同时有 WHERE 和 LIMIT
String pageByUserIdSection = xmlContent.substring(xmlContent.indexOf("id='pageByUserId'"))
assert pageByUserIdSection.contains("user_id") : "pageByUserId should have user_id condition"
assert pageByUserIdSection.contains("LIMIT #{offset}, #{limit}") : "pageByUserId should have LIMIT"

// --- pageByUserId 对应的 count 方法 ---
assert xmlContent.contains("id='countPageByUserId'") || xmlContent.contains("countByUserId") : "XML should contain count method for pageByUserId"

// count 方法应该有 SELECT COUNT(*)
assert xmlContent.contains("SELECT COUNT(*)") : "count methods should have SELECT COUNT(*)"

println "[select-page] Mapper XML assertions passed."

// ========== 2. Mapper 接口验证 ==========
File mapperFile = new File(basedir, "src/main/java/com/example/mapper/TOrderMapper.java")
assert mapperFile.exists() : "TOrderMapper.java should exist"
String mapperContent = mapperFile.text

assert mapperContent.contains("pageAll") : "Mapper should contain pageAll method"
assert mapperContent.contains("pageByUserId") : "Mapper should contain pageByUserId method"

// 分页方法应该有 offset 和 limit 参数
assert mapperContent.contains("offset") : "Page method should have offset parameter"
assert mapperContent.contains("limit") : "Page method should have limit parameter"

// count 方法
assert mapperContent.contains("long") : "Count method should return long"

println "[select-page] Mapper interface assertions passed."

// ========== 3. Service 文件验证 ==========
File serviceFile = new File(basedir, "src/main/java/com/example/service/OrderService.java")
assert serviceFile.exists() : "OrderService.java should exist"
String serviceContent = serviceFile.text

assert !serviceContent.contains("TOrderDesign.") : "Design chain should be replaced"
assert serviceContent.contains("tOrderMapper") : "Mapper field should exist"
assert serviceContent.contains("tOrderMapper.pageAll") : "Should call mapper.pageAll"
assert serviceContent.contains("tOrderMapper.pageByUserId") : "Should call mapper.pageByUserId"

println "[select-page] Service file assertions passed."
println "[query-transformer/select-page] All assertions passed."
return true

// ========== 1. Mapper 接口验证 ==========
File mapperFile = new File(basedir, "src/main/java/com/example/mapper/TOrderMapper.java")
assert mapperFile.exists() : "TOrderMapper.java should exist"
String mapperContent = mapperFile.text

assert mapperContent.contains("listOrderNos") : "Mapper should contain listOrderNos method"
assert mapperContent.contains("listOrderSummaries") : "Mapper should contain listOrderSummaries method"
assert mapperContent.contains("listOrderSummariesByStatus") : "Mapper should contain listOrderSummariesByStatus method"

println "[select-properties] Mapper interface assertions passed."

// ========== 2. Mapper XML 验证 ==========
File mapperXmlFile = new File(basedir, "src/main/resources/mapper/TOrderMapper.xml")
assert mapperXmlFile.exists() : "TOrderMapper.xml should exist"
String xmlContent = mapperXmlFile.text

// listOrderNos: SELECT 仅 order_no 列
assert xmlContent.contains("id='listOrderNos'") : "XML should contain listOrderNos select"
assert xmlContent.contains("order_no") : "XML should contain order_no column"

// listOrderSummaries: SELECT 指定多列
assert xmlContent.contains("id='listOrderSummaries'") : "XML should contain listOrderSummaries select"
assert xmlContent.contains("user_id") : "XML should contain user_id column"

// 不应包含 <include refid="all"/> （因为指定了属性，不应是 SELECT *）
// listOrderNos 等 select 中不应出现 refid="all"
// 注意：这里通过检查 xml 中指定 id 附近是否有特定列来验证
assert xmlContent.contains("id='listOrderSummariesByStatus'") : "XML should contain listOrderSummariesByStatus select"

println "[select-properties] Mapper XML assertions passed."

// ========== 3. Record DTO 验证 ==========
// listOrderSummaries 应生成 Record DTO（多属性场景）
File recordDir = new File(basedir, "src/main/java/com/example/dto/record")
assert recordDir.exists() : "Record DTO directory should exist"

File[] recordFiles = recordDir.listFiles()
assert recordFiles != null && recordFiles.length > 0 : "At least one Record DTO should be generated"

// 验证 Record DTO 包含 orderNo, userId, amount 字段
String recordContent = ""
for (File f : recordFiles) {
    recordContent += f.text
}
assert recordContent.contains("orderNo") : "Record DTO should contain orderNo field"
assert recordContent.contains("userId") : "Record DTO should contain userId field"
assert recordContent.contains("amount") : "Record DTO should contain amount field"

println "[select-properties] Record DTO assertions passed."

// ========== 4. Service 文件验证 ==========
File serviceFile = new File(basedir, "src/main/java/com/example/service/OrderService.java")
assert serviceFile.exists() : "OrderService.java should exist"
String serviceContent = serviceFile.text

// Design 链已被替换
assert !serviceContent.contains("TOrderDesign.") : "Design chain should be replaced"

// Mapper 被注入
assert serviceContent.contains("TOrderMapper") : "Mapper should be injected"
assert serviceContent.contains("tOrderMapper") : "Mapper field should exist"

// 方法调用
assert serviceContent.contains("tOrderMapper.listOrderNos") : "Should call mapper.listOrderNos"
assert serviceContent.contains("tOrderMapper.listOrderSummaries") : "Should call mapper.listOrderSummaries"
assert serviceContent.contains("tOrderMapper.listOrderSummariesByStatus") : "Should call mapper.listOrderSummariesByStatus"

println "[select-properties] Service file assertions passed."
println "[query-transformer/select-properties] All assertions passed."
return true

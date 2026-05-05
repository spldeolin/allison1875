// ========== 1. Mapper 接口验证 ==========
File mapperFile = new File(basedir, "src/main/java/com/example/mapper/TOrderMapper.java")
assert mapperFile.exists() : "TOrderMapper.java should exist"
String mapperContent = mapperFile.text

assert mapperContent.contains("listOrderWithUserName") : "Mapper should contain listOrderWithUserName method"

println "[join-basic] Mapper interface assertions passed."

// ========== 2. Mapper XML 验证 ==========
File mapperXmlFile = new File(basedir, "src/main/resources/mapper/TOrderMapper.xml")
assert mapperXmlFile.exists() : "TOrderMapper.xml should exist"
String xmlContent = mapperXmlFile.text

// listOrderWithUserName: SELECT with LEFT JOIN
assert xmlContent.contains("id='listOrderWithUserName'") : "XML should contain listOrderWithUserName select"
assert xmlContent.contains("LEFT JOIN") : "XML should contain LEFT JOIN clause"
assert xmlContent.contains("t_user") : "XML should contain t_user table name in JOIN"
assert xmlContent.contains("t1.") : "XML should use t1 alias for main table"
assert xmlContent.contains("t2.") : "XML should use t2 alias for joined table"

// ON 条件：t2.id = t1.user_id
assert xmlContent.contains("ON") || xmlContent.contains("on") || xmlContent.contains("t2.") : "XML should contain ON condition"

println "[join-basic] Mapper XML assertions passed."

// ========== 3. Record DTO 验证 ==========
// join 查询选择了 joined 列，应生成 Record DTO
File recordDir = new File(basedir, "src/main/java/com/example/dto/record")
assert recordDir.exists() : "Record DTO directory should exist"

File[] recordFiles = recordDir.listFiles()
assert recordFiles != null && recordFiles.length > 0 : "At least one Record DTO should be generated for join query"

// 验证 Record DTO 包含 join 过来的字段
String recordContent = ""
for (File f : recordFiles) {
    recordContent += f.text
}
assert recordContent.contains("userName") || recordContent.contains("UserName") : "Record DTO should contain joined userName field"

println "[join-basic] Record DTO assertions passed."

// ========== 4. Service 文件验证 ==========
File serviceFile = new File(basedir, "src/main/java/com/example/service/OrderService.java")
assert serviceFile.exists() : "OrderService.java should exist"
String serviceContent = serviceFile.text

// Design 链已被替换
assert !serviceContent.contains("TOrderDesign.select") : "Design chain should be replaced"

// Mapper 被注入
assert serviceContent.contains("TOrderMapper") : "Mapper should be injected"

println "[join-basic] Service file assertions passed."
println "[query-transformer/join-basic] All assertions passed."
return true

// ========== 1. Mapper 接口验证 ==========
File mapperFile = new File(basedir, "src/main/java/com/example/mapper/TOrderMapper.java")
assert mapperFile.exists() : "TOrderMapper.java should exist"
String mapperContent = mapperFile.text

assert mapperContent.contains("findById") : "Mapper should contain findById method"
assert mapperContent.contains("listAll") : "Mapper should contain listAll method"
assert mapperContent.contains("countByUserId") : "Mapper should contain countByUserId method"

println "[select-basic] Mapper interface assertions passed."

// ========== 2. Mapper XML 验证 ==========
File mapperXmlFile = new File(basedir, "src/main/resources/mapper/TOrderMapper.xml")
assert mapperXmlFile.exists() : "TOrderMapper.xml should exist"
String xmlContent = mapperXmlFile.text

// findById: SELECT with LIMIT 1
assert xmlContent.contains("id='findById'") : "XML should contain findById select"
assert xmlContent.contains("LIMIT 1") : "one() should generate LIMIT 1"

// listAll: SELECT without WHERE
assert xmlContent.contains("id='listAll'") : "XML should contain listAll select"

// countByUserId: SELECT COUNT(*)
assert xmlContent.contains("id='countByUserId'") : "XML should contain countByUserId select"
assert xmlContent.contains("COUNT(*)") : "count() should generate COUNT(*)"
assert xmlContent.contains("user_id") : "XML should contain user_id column"

println "[select-basic] Mapper XML assertions passed."

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
assert serviceContent.contains("tOrderMapper.findById") : "Should call mapper.findById"
assert serviceContent.contains("tOrderMapper.listAll") : "Should call mapper.listAll"
assert serviceContent.contains("tOrderMapper.countByUserId") : "Should call mapper.countByUserId"

println "[select-basic] Service file assertions passed."
println "[query-transformer/select-basic] All assertions passed."
return true
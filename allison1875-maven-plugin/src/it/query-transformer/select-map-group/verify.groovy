// ========== 1. Mapper 接口验证 ==========
File mapperFile = new File(basedir, "src/main/java/com/example/mapper/TOrderMapper.java")
assert mapperFile.exists() : "TOrderMapper.java should exist"
String mapperContent = mapperFile.text

assert mapperContent.contains("mapByUserId") : "Mapper should contain mapByUserId method"
assert mapperContent.contains("groupByStatus") : "Mapper should contain groupByStatus method"
assert mapperContent.contains("mapByUserIdWithCondition") : "Mapper should contain mapByUserIdWithCondition method"

// mapByUserId 方法应有 @MapKey 注解
assert mapperContent.contains("@MapKey") : "MAP return style should generate @MapKey annotation"
assert mapperContent.contains("\"userId\"") : "@MapKey should reference userId"

println "[select-map-group] Mapper interface assertions passed."

// ========== 2. Mapper XML 验证 ==========
File mapperXmlFile = new File(basedir, "src/main/resources/mapper/TOrderMapper.xml")
assert mapperXmlFile.exists() : "TOrderMapper.xml should exist"
String xmlContent = mapperXmlFile.text

// mapByUserId: SELECT without WHERE
assert xmlContent.contains("id='mapByUserId'") : "XML should contain mapByUserId select"

// groupByStatus: SELECT without WHERE
assert xmlContent.contains("id='groupByStatus'") : "XML should contain groupByStatus select"

// mapByUserIdWithCondition: SELECT with WHERE status =
assert xmlContent.contains("id='mapByUserIdWithCondition'") : "XML should contain mapByUserIdWithCondition select"
assert xmlContent.contains("status") : "XML should contain status condition"

println "[select-map-group] Mapper XML assertions passed."

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
assert serviceContent.contains("tOrderMapper.mapByUserId") : "Should call mapper.mapByUserId"
assert serviceContent.contains("tOrderMapper.groupByStatus") : "Should call mapper.groupByStatus"
assert serviceContent.contains("tOrderMapper.mapByUserIdWithCondition") : "Should call mapper.mapByUserIdWithCondition"

// groupByStatus 应有 Collectors.groupingBy 调用
assert serviceContent.contains("groupingBy") : "GROUP should generate Collectors.groupingBy call"

println "[select-map-group] Service file assertions passed."
println "[query-transformer/select-map-group] All assertions passed."
return true

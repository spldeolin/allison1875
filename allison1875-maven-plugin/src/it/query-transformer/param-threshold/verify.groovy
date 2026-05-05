/*
 * query-transformer param-threshold 集成测试验证脚本
 *
 * 验证当 where 条件超过 3 个时自动生成 ParamDTO 类，
 * Mapper 方法参数变为单个 DTO 对象，Service 中包含 ParamDTO 构建代码。
 */

// ========== 1. ParamDTO 文件验证 ==========
File paramDTODir = new File(basedir, "src/main/java/com/example/dto/param")
assert paramDTODir.exists() : "paramDTO package directory should exist"

// 查找生成的 ParamDTO 文件
def paramFiles = []
paramDTODir.eachFileRecurse(groovy.io.FileType.FILES) { file ->
    if (file.name.endsWith(".java") && file.name.contains("Param")) {
        paramFiles << file
    }
}
assert paramFiles.size() > 0 : "At least one ParamDTO java file should be generated"

// 验证 ParamDTO 内容：应包含 4 个字段
String paramContent = paramFiles[0].text
assert paramContent.contains("orderNo") : "ParamDTO should contain orderNo field"
assert paramContent.contains("userId") : "ParamDTO should contain userId field"
assert paramContent.contains("status") : "ParamDTO should contain status field"
assert paramContent.contains("amount") : "ParamDTO should contain amount field"
println "[param-threshold] ParamDTO file assertions passed: ${paramFiles[0].name}"

// ========== 2. Mapper 接口验证 ==========
File mapperFile = new File(basedir, "src/main/java/com/example/mapper/TOrderMapper.java")
assert mapperFile.exists() : "TOrderMapper.java should exist"
String mapperContent = mapperFile.text

assert mapperContent.contains("listByMultipleConditions") : "Mapper should contain listByMultipleConditions method"
// 验证 query-transformer 生成的方法参数是单个 DTO 类型，而不是多个 @Param
// 注意：persistence-generator 生成的基础方法仍然可能使用 @Param，只检查目标方法行
String listMethodLine = mapperContent.readLines().find { it.contains("listByMultipleConditions") }
assert listMethodLine != null : "listByMultipleConditions method line should exist"
assert !listMethodLine.contains("@Param") : "ParamDTO method should NOT have @Param in its signature"
assert listMethodLine.contains("ListByMultipleConditionsParam") : "Method param should be the ParamDTO type"

println "[param-threshold] Mapper interface assertions passed."

// ========== 3. Mapper XML 验证 ==========
File mapperXmlFile = new File(basedir, "src/main/resources/mapper/TOrderMapper.xml")
assert mapperXmlFile.exists() : "TOrderMapper.xml should exist"
String xmlContent = mapperXmlFile.text

assert xmlContent.contains("id='listByMultipleConditions'") : "XML should contain listByMultipleConditions select"
// 验证 XML 中使用了 ParamDTO 的字段引用
assert xmlContent.contains("#{orderNo}") : "XML should reference #{orderNo}"
assert xmlContent.contains("#{userId}") : "XML should reference #{userId}"
assert xmlContent.contains("#{status}") : "XML should reference #{status}"
assert xmlContent.contains("#{amount}") : "XML should reference #{amount}"

println "[param-threshold] Mapper XML assertions passed."

// ========== 4. Service 文件验证 ==========
File serviceFile = new File(basedir, "src/main/java/com/example/service/OrderService.java")
assert serviceFile.exists() : "OrderService.java should exist"
String serviceContent = serviceFile.text

// DSL 应该被替换掉
assert !serviceContent.contains("TOrderDesign.") : "Design chain should be replaced"
// 应包含 ParamDTO 构建代码（new 和 setter 调用）
assert serviceContent.contains("Param") : "Service should reference ParamDTO type"
assert serviceContent.contains(".setOrderNo(") : "Service should set orderNo on ParamDTO"
assert serviceContent.contains(".setUserId(") : "Service should set userId on ParamDTO"
assert serviceContent.contains(".setStatus(") : "Service should set status on ParamDTO"
assert serviceContent.contains(".setAmount(") : "Service should set amount on ParamDTO"

println "[param-threshold] Service file assertions passed."
println "[query-transformer/param-threshold] All assertions passed."
return true

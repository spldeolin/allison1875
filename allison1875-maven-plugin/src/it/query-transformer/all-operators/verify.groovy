/*
 * query-transformer all-operators 集成测试验证脚本
 *
 * 验证 query-transformer 处理所有11种比较运算符时，生成正确的 Mapper XML SQL。
 */

// ========== 1. Mapper XML 验证 ==========
File mapperXmlFile = new File(basedir, "src/main/resources/mapper/TOrderMapper.xml")
assert mapperXmlFile.exists() : "TOrderMapper.xml should exist"
String xmlContent = mapperXmlFile.text

// --- eq运算符：= ---
assert xmlContent.contains("id='queryByEq'") : "XML should contain queryByEq"
assert xmlContent.contains("id = #{id}") : "eq should generate '= #{id}'"

// --- ne运算符：!= ---
assert xmlContent.contains("id='queryByNe'") : "XML should contain queryByNe"
assert xmlContent.contains("status != #{status}") : "ne should generate '!= #{status}'"

// --- gt运算符：> ---
assert xmlContent.contains("id='queryByGt'") : "XML should contain queryByGt"
assert xmlContent.contains("amount > #{amount}") || xmlContent.contains("amount > #{minAmount}") : "gt should generate '> #{var}'"

// --- ge运算符：>= ---
assert xmlContent.contains("id='queryByGe'") : "XML should contain queryByGe"
assert xmlContent.contains("amount >= #{amount}") || xmlContent.contains("amount >= #{minAmount}") : "ge should generate '>= #{var}'"

// --- lt运算符：< (XML escaped as &lt;) ---
assert xmlContent.contains("id='queryByLt'") : "XML should contain queryByLt"
assert xmlContent.contains("amount &lt; #{amount}") || xmlContent.contains("amount &lt; #{maxAmount}") : "lt should generate '< #{var}' (escaped)"

// --- le运算符：<= (XML escaped as &lt;=) ---
assert xmlContent.contains("id='queryByLe'") : "XML should contain queryByLe"
assert xmlContent.contains("amount &lt;= #{amount}") || xmlContent.contains("amount &lt;= #{maxAmount}") : "le should generate '<= #{var}' (escaped)"

// --- like运算符：LIKE CONCAT ---
assert xmlContent.contains("id='queryByLike'") : "XML should contain queryByLike"
assert xmlContent.contains("LIKE CONCAT('%',") : "like should generate LIKE CONCAT pattern"
assert xmlContent.contains("order_no") : "like should reference order_no column"

// --- in运算符：IN (<foreach>) ---
assert xmlContent.contains("id='queryByIn'") : "XML should contain queryByIn"
assert xmlContent.contains("status IN (") : "in should generate 'column IN (...)'"
assert xmlContent.contains("foreach") : "in should use foreach for collection"

// --- nin运算符：NOT IN (<foreach>) ---
assert xmlContent.contains("id='queryByNin'") : "XML should contain queryByNin"
assert xmlContent.contains("status NOT IN (") : "nin should generate 'column NOT IN (...)'"

// --- notnull运算符：IS NOT NULL ---
assert xmlContent.contains("id='queryByNotnull'") : "XML should contain queryByNotnull"
assert xmlContent.contains("remark IS NOT NULL") : "notnull should generate 'IS NOT NULL'"

// --- isnull运算符：IS NULL ---
assert xmlContent.contains("id='queryByIsnull'") : "XML should contain queryByIsnull"
assert xmlContent.contains("remark IS NULL") : "isnull should generate 'IS NULL'"

println "[all-operators] Mapper XML operator assertions passed."

// ========== 2. Mapper 接口验证 ==========
File mapperFile = new File(basedir, "src/main/java/com/example/mapper/TOrderMapper.java")
assert mapperFile.exists() : "TOrderMapper.java should exist"
String mapperContent = mapperFile.text

assert mapperContent.contains("queryByEq") : "Mapper should contain queryByEq method"
assert mapperContent.contains("queryByNe") : "Mapper should contain queryByNe method"
assert mapperContent.contains("queryByGt") : "Mapper should contain queryByGt method"
assert mapperContent.contains("queryByGe") : "Mapper should contain queryByGe method"
assert mapperContent.contains("queryByLt") : "Mapper should contain queryByLt method"
assert mapperContent.contains("queryByLe") : "Mapper should contain queryByLe method"
assert mapperContent.contains("queryByLike") : "Mapper should contain queryByLike method"
assert mapperContent.contains("queryByIn") : "Mapper should contain queryByIn method"
assert mapperContent.contains("queryByNin") : "Mapper should contain queryByNin method"
assert mapperContent.contains("queryByNotnull") : "Mapper should contain queryByNotnull method"
assert mapperContent.contains("queryByIsnull") : "Mapper should contain queryByIsnull method"

println "[all-operators] Mapper interface assertions passed."

// ========== 3. Service 文件验证 ==========
File serviceFile = new File(basedir, "src/main/java/com/example/service/OrderService.java")
assert serviceFile.exists() : "OrderService.java should exist"
String serviceContent = serviceFile.text

// Design 链已被替换
assert !serviceContent.contains("TOrderDesign.") : "Design chain should be replaced"

// Mapper 被注入
assert serviceContent.contains("TOrderMapper") : "Mapper should be injected"
assert serviceContent.contains("tOrderMapper") : "Mapper field should exist"

// 所有方法调用
assert serviceContent.contains("tOrderMapper.queryByEq") : "Should call mapper.queryByEq"
assert serviceContent.contains("tOrderMapper.queryByNe") : "Should call mapper.queryByNe"
assert serviceContent.contains("tOrderMapper.queryByGt") : "Should call mapper.queryByGt"
assert serviceContent.contains("tOrderMapper.queryByGe") : "Should call mapper.queryByGe"
assert serviceContent.contains("tOrderMapper.queryByLt") : "Should call mapper.queryByLt"
assert serviceContent.contains("tOrderMapper.queryByLe") : "Should call mapper.queryByLe"
assert serviceContent.contains("tOrderMapper.queryByLike") : "Should call mapper.queryByLike"
assert serviceContent.contains("tOrderMapper.queryByIn") : "Should call mapper.queryByIn"
assert serviceContent.contains("tOrderMapper.queryByNin") : "Should call mapper.queryByNin"
assert serviceContent.contains("tOrderMapper.queryByNotnull") : "Should call mapper.queryByNotnull"
assert serviceContent.contains("tOrderMapper.queryByIsnull") : "Should call mapper.queryByIsnull"

println "[all-operators] Service file assertions passed."
println "[query-transformer/all-operators] All assertions passed."
return true

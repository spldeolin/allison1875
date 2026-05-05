/*
 * persistence-generator existing-mapper 集成测试验证脚本
 *
 * 验证已存在的 Mapper 接口中自定义方法被保留，同时基础方法被重新生成
 */

File mapperFile = new File(basedir, "src/main/java/com/example/mapper/TAccountMapper.java")
assert mapperFile.exists() : "TAccountMapper.java should exist"
String mapperContent = mapperFile.text

// 自定义方法应被保留
assert mapperContent.contains("queryByBalanceRange") : "Custom method queryByBalanceRange should be preserved"
assert mapperContent.contains("sumBalance") : "Custom method sumBalance should be preserved"

// 基础方法应被生成
assert mapperContent.contains("insert") : "Generated method insert should exist"
assert mapperContent.contains("queryById") : "Generated method queryById should exist"
assert mapperContent.contains("updateById") : "Generated method updateById should exist"
assert mapperContent.contains("deleteById") : "Generated method deleteById should exist"
assert mapperContent.contains("queryByAccountName") : "Generated method queryByAccountName should exist"

// 自定义方法应在 Mapper 的末尾（在基础方法之后）
int queryByBalanceRangePos = mapperContent.indexOf("queryByBalanceRange")
int insertPos = mapperContent.indexOf("int insert")
assert insertPos < queryByBalanceRangePos : "Custom methods should be after generated methods"

println "[existing-mapper] Mapper assertions passed."

println "[persistence-generator/existing-mapper] All assertions passed."
return true

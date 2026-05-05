/*
 * persistence-generator existing-xml-markers 集成测试验证脚本
 *
 * 验证已存在包含 [START]/[END] 标记的 Mapper XML：
 * 1. 标记区间内容被替换为新生成的方法
 * 2. 标记区间外的自定义 SQL 被保留
 * 3. 旧的生成内容（oldMethod）被清除
 */

File xmlFile = new File(basedir, "src/main/resources/mapper/TPaymentMapper.xml")
assert xmlFile.exists() : "TPaymentMapper.xml should exist"
String xmlContent = xmlFile.text

// 旧的生成内容应该被清除
assert !xmlContent.contains("oldMethod") : "Old generated content (oldMethod) should be removed"

// 新的基础方法应该被生成
assert xmlContent.contains("resultMap") : "New resultMap should be generated"
assert xmlContent.contains("insert") : "New insert method should be generated"
assert xmlContent.contains("queryById") : "New queryById method should be generated"

// 自定义 SQL 应该被保留
assert xmlContent.contains("customQueryByStatus") : "Custom SQL customQueryByStatus should be preserved"
assert xmlContent.contains("SELECT * FROM t_payment WHERE status") : "Custom SQL body should be preserved"

// [START] 标记应该存在（新生成的区间）
assert xmlContent.contains("[START]") : "Generated content should have [START] marker"
assert xmlContent.contains("[END]") : "Generated content should have [END] marker"

println "[existing-xml-markers] XML marker replacement assertions passed."

println "[persistence-generator/existing-xml-markers] All assertions passed."
return true

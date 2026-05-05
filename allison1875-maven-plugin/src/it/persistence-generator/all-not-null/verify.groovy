/*
 * persistence-generator all-not-null 集成测试验证脚本
 *
 * 验证所有字段均 NOT NULL 时，不生成 updateByIdEvenNull 方法
 */

File mapperFile = new File(basedir, "src/main/java/com/example/mapper/TStrictMapper.java")
assert mapperFile.exists() : "TStrictMapper.java should be generated"
String mapperContent = mapperFile.text

// 不应该生成 updateByIdEvenNull（因为所有字段都是 NOT NULL，不存在需要设null的场景）
assert !mapperContent.contains("updateByIdEvenNull") : "Should NOT generate updateByIdEvenNull when all fields are NOT NULL"

// 仍应生成 updateById
assert mapperContent.contains("updateById") : "Should still generate updateById"

// 基础方法仍应存在
assert mapperContent.contains("insert") : "Should generate insert"
assert mapperContent.contains("queryById") : "Should generate queryById"
assert mapperContent.contains("deleteById") : "Should generate deleteById"

println "[all-not-null] Mapper assertions passed."

// 验证 XML 也不包含 updateByIdEvenNull
File xmlFile = new File(basedir, "src/main/resources/mapper/TStrictMapper.xml")
assert xmlFile.exists() : "TStrictMapper.xml should be generated"
String xmlContent = xmlFile.text
assert !xmlContent.contains("updateByIdEvenNull") : "XML should NOT contain updateByIdEvenNull"

println "[all-not-null] XML assertions passed."

println "[persistence-generator/all-not-null] All assertions passed."
return true

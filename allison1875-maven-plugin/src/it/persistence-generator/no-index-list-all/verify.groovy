/*
 * persistence-generator no-index-list-all 集成测试验证脚本
 *
 * 验证表无索引（仅主键）时，生成 listAll 方法
 */

File mapperFile = new File(basedir, "src/main/java/com/example/mapper/TConfigMapper.java")
assert mapperFile.exists() : "TConfigMapper.java should be generated"
String mapperContent = mapperFile.text

// 无索引时应该生成 listAll
assert mapperContent.contains("listAll") : "Mapper should contain listAll for table without indexes"

// 不应该有 queryByXxx 方法（因为无索引）
assert !mapperContent.contains("queryByConfigKey") : "Should NOT have queryByConfigKey (no index on config_key)"

println "[no-index-list-all] Mapper assertions passed."

// 验证 XML 中生成 listAll
File xmlFile = new File(basedir, "src/main/resources/mapper/TConfigMapper.xml")
assert xmlFile.exists() : "TConfigMapper.xml should be generated"
String xmlContent = xmlFile.text
assert xmlContent.contains("listAll") : "XML should contain listAll select"
assert xmlContent.contains("SELECT <include refid=\"all\"/>") : "listAll should select all columns"

println "[no-index-list-all] XML assertions passed."

println "[persistence-generator/no-index-list-all] All assertions passed."
return true

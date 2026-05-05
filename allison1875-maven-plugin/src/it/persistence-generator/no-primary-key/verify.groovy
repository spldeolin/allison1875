/*
 * persistence-generator no-primary-key 集成测试验证脚本
 *
 * 验证无主键表：不生成 queryById/updateById/deleteById/queryByIds/queryByIdsEachId
 */

File mapperFile = new File(basedir, "src/main/java/com/example/mapper/TLogMapper.java")
assert mapperFile.exists() : "TLogMapper.java should be generated"
String mapperContent = mapperFile.text

// 不应该生成依赖主键的方法
assert !mapperContent.contains("queryById") : "Should NOT generate queryById for table without PK"
assert !mapperContent.contains("updateById") : "Should NOT generate updateById for table without PK"
assert !mapperContent.contains("deleteById") : "Should NOT generate deleteById for table without PK"
assert !mapperContent.contains("queryByIds") : "Should NOT generate queryByIds for table without PK"
assert !mapperContent.contains("queryByIdsEachId") : "Should NOT generate queryByIdsEachId for table without PK"

// 应该生成 insert 和 batchInsert
assert mapperContent.contains("insert") : "Should still generate insert"
assert mapperContent.contains("batchInsert") : "Should still generate batchInsert"

// 无索引也无主键，应该生成 listAll
assert mapperContent.contains("listAll") : "Should generate listAll for table without indexes"

println "[no-primary-key] Mapper assertions passed."

// 验证 XML
File xmlFile = new File(basedir, "src/main/resources/mapper/TLogMapper.xml")
assert xmlFile.exists() : "TLogMapper.xml should be generated"
String xmlContent = xmlFile.text
assert !xmlContent.contains("queryById") : "XML should NOT contain queryById"
assert !xmlContent.contains("deleteById") : "XML should NOT contain deleteById"
assert xmlContent.contains("insert") : "XML should contain insert"
assert xmlContent.contains("listAll") : "XML should contain listAll"

// resultMap 不应该有 <id> 标签
assert !xmlContent.contains("<id column=") : "ResultMap should NOT have <id> for table without PK"

println "[no-primary-key] XML assertions passed."

// 验证 Entity
File entityFile = new File(basedir, "src/main/java/com/example/entity/TLogEntity.java")
assert entityFile.exists() : "TLogEntity.java should be generated"

println "[persistence-generator/no-primary-key] All assertions passed."
return true

/*
 * persistence-generator no-modify-announce-off 集成测试验证脚本
 *
 * 验证 enableNoModifyAnnounce=false 时，生成代码不包含 NO_MODIFY_ANNOUNCE 注释
 */

String NO_MODIFY_ANNOUNCE = "Any modifications may be overwritten by future code generations."

// 验证 Entity 不包含 NO_MODIFY_ANNOUNCE
File entityFile = new File(basedir, "src/main/java/com/example/entity/TTagEntity.java")
assert entityFile.exists() : "TTagEntity.java should be generated"
String entityContent = entityFile.text
assert !entityContent.contains(NO_MODIFY_ANNOUNCE) : "Entity should NOT contain NO_MODIFY_ANNOUNCE"

println "[no-modify-announce-off] Entity assertion passed."

// 验证 Mapper 不包含 NO_MODIFY_ANNOUNCE
File mapperFile = new File(basedir, "src/main/java/com/example/mapper/TTagMapper.java")
assert mapperFile.exists() : "TTagMapper.java should be generated"
String mapperContent = mapperFile.text
assert !mapperContent.contains(NO_MODIFY_ANNOUNCE) : "Mapper should NOT contain NO_MODIFY_ANNOUNCE"

println "[no-modify-announce-off] Mapper assertion passed."

// 验证 XML 不包含 NO_MODIFY_ANNOUNCE
File xmlFile = new File(basedir, "src/main/resources/mapper/TTagMapper.xml")
assert xmlFile.exists() : "TTagMapper.xml should be generated"
String xmlContent = xmlFile.text
assert !xmlContent.contains(NO_MODIFY_ANNOUNCE) : "XML should NOT contain NO_MODIFY_ANNOUNCE"

println "[no-modify-announce-off] XML assertion passed."

println "[persistence-generator/no-modify-announce-off] All assertions passed."
return true

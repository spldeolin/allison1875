/*
 * persistence-generator composite-primary-key 集成测试验证脚本
 *
 * 验证复合主键表：
 * 1. queryById/deleteById 生成多参数方法（@Param注解）
 * 2. 不生成 queryByIds/queryByIdsEachId（因为 idProperties.size() != 1）
 */

File mapperFile = new File(basedir, "src/main/java/com/example/mapper/TUserRoleMapper.java")
assert mapperFile.exists() : "TUserRoleMapper.java should be generated"
String mapperContent = mapperFile.text

// queryById 应该有多个 @Param 注解的参数
assert mapperContent.contains("queryById") : "Mapper should contain queryById"
assert mapperContent.contains("@Param") : "queryById should use @Param for composite key"
assert mapperContent.contains("userId") : "queryById should have userId param"
assert mapperContent.contains("roleId") : "queryById should have roleId param"

// 不应该生成 queryByIds（因为复合主键）
assert !mapperContent.contains("queryByIds") : "Should NOT generate queryByIds for composite PK"
assert !mapperContent.contains("queryByIdsEachId") : "Should NOT generate queryByIdsEachId for composite PK"

// deleteById 也应该有多参数
assert mapperContent.contains("deleteById") : "Mapper should contain deleteById"

println "[composite-primary-key] Mapper assertions passed."

// 验证 XML 中 queryById/deleteById 对应多字段
File xmlFile = new File(basedir, "src/main/resources/mapper/TUserRoleMapper.xml")
assert xmlFile.exists() : "TUserRoleMapper.xml should be generated"
String xmlContent = xmlFile.text
assert xmlContent.contains("user_id = #{userId}") : "XML queryById should match user_id"
assert xmlContent.contains("role_id = #{roleId}") : "XML queryById should match role_id"

println "[composite-primary-key] XML assertions passed."

// 验证 Entity
File entityFile = new File(basedir, "src/main/java/com/example/entity/TUserRoleEntity.java")
assert entityFile.exists() : "TUserRoleEntity.java should be generated"
String entityContent = entityFile.text
assert entityContent.contains("userId") : "Entity should have userId"
assert entityContent.contains("roleId") : "Entity should have roleId"
assert entityContent.contains("grantedAt") : "Entity should have grantedAt"

println "[persistence-generator/composite-primary-key] All assertions passed."
return true

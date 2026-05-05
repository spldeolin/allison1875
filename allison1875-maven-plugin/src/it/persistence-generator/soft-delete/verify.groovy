/*
 * persistence-generator soft-delete 集成测试验证脚本
 *
 * 验证配置逻辑删除后：
 * 1. deleteById 生成 UPDATE 而非 DELETE
 * 2. 查询方法包含未删除条件 (is_deleted = 0)
 * 3. deleteByIndex 方法也使用 UPDATE
 */

// ========== 1. 验证 Mapper XML 中 deleteById 使用 UPDATE ==========
File xmlFile = new File(basedir, "src/main/resources/mapper/TArticleMapper.xml")
assert xmlFile.exists() : "TArticleMapper.xml should be generated"
String xmlContent = xmlFile.text

// deleteById 应该生成 <update> 而不是 <delete>
assert xmlContent.contains("<update id=\"deleteById\"") : "deleteById should use <update> tag for soft delete"
assert xmlContent.contains("SET is_deleted = 1") : "deleteById should SET is_deleted = 1"
assert !xmlContent.contains("<delete id=\"deleteById\"") : "deleteById should NOT use <delete> tag"

println "[soft-delete] deleteById uses UPDATE assertion passed."

// ========== 2. 验证查询方法包含未删除条件 ==========
assert xmlContent.contains("is_deleted = 0") : "Query methods should include is_deleted = 0 condition"

// queryById 应该包含 is_deleted = 0
// 找到 queryById 相关内容
assert xmlContent.contains("queryById") : "Should contain queryById method"

println "[soft-delete] Not-deleted condition assertion passed."

// ========== 3. 验证 deleteByAuthorId 也使用 UPDATE ==========
assert xmlContent.contains("<update id=\"deleteByAuthorId\"") : "deleteByAuthorId should use <update> tag"

println "[soft-delete] deleteByIndex uses UPDATE assertion passed."

// ========== 4. 验证 Entity 生成 ==========
File entityFile = new File(basedir, "src/main/java/com/example/entity/TArticleEntity.java")
assert entityFile.exists() : "TArticleEntity.java should be generated"
String entityContent = entityFile.text
assert entityContent.contains("isDeleted") : "Entity should contain isDeleted field"
assert entityContent.contains("content") : "Entity should contain content field"

println "[soft-delete] Entity assertion passed."

// ========== 5. 验证 Mapper 接口 ==========
File mapperFile = new File(basedir, "src/main/java/com/example/mapper/TArticleMapper.java")
assert mapperFile.exists() : "TArticleMapper.java should be generated"
String mapperContent = mapperFile.text
assert mapperContent.contains("deleteById") : "Mapper should contain deleteById"
assert mapperContent.contains("queryByAuthorId") : "Mapper should contain queryByAuthorId"

println "[soft-delete] Mapper assertion passed."

println "[persistence-generator/soft-delete] All assertions passed."
return true

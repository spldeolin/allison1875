/*
 * persistence-generator offset-limit-page-style 集成测试验证脚本
 *
 * 验证 pageParamStyle=OFFSET_LIMIT 时，Design 中分页方法使用 offset/limit 参数名
 */

File designDir = new File(basedir, "src/main/java/com/example/design")
assert designDir.exists() : "design directory should exist"

File designFile = new File(designDir, "TMessageDesign.java")
assert designFile.exists() : "TMessageDesign.java should be generated"
String designContent = designFile.text

// 分页方法应该使用 offset/limit 参数
assert designContent.contains("offset") : "Design page method should use 'offset' parameter"
assert designContent.contains("limit") : "Design page method should use 'limit' parameter"

// 不应该使用 pageNo/pageSize
assert !designContent.contains("pageNo") : "Design should NOT use 'pageNo' when style is OFFSET_LIMIT"
assert !designContent.contains("pageSize") : "Design should NOT use 'pageSize' when style is OFFSET_LIMIT"

println "[offset-limit-page-style] Design assertions passed."

// Entity 和 Mapper 不受影响，仍应正常生成
File entityFile = new File(basedir, "src/main/java/com/example/entity/TMessageEntity.java")
assert entityFile.exists() : "TMessageEntity.java should be generated"

File mapperFile = new File(basedir, "src/main/java/com/example/mapper/TMessageMapper.java")
assert mapperFile.exists() : "TMessageMapper.java should be generated"

println "[persistence-generator/offset-limit-page-style] All assertions passed."
return true

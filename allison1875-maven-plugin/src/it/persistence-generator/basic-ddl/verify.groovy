/*
 * persistence-generator basic-ddl 集成测试验证脚本
 *
 * 验证 persistence-generator 通过 DDL 解析表结构后生成：
 * 1. Entity Java 文件
 * 2. Mapper 接口
 * 3. Mapper XML 文件
 * 4. Design 文件
 */

// ========== 1. 验证 Entity 文件生成 ==========
File entityFile = new File(basedir, "src/main/java/com/example/entity/TOrderEntity.java")
assert entityFile.exists() : "TOrderEntity.java should be generated"
String entityContent = entityFile.text

// 验证字段存在
assert entityContent.contains("id") : "Entity should contain field 'id'"
assert entityContent.contains("orderNo") : "Entity should contain field 'orderNo'"
assert entityContent.contains("userId") : "Entity should contain field 'userId'"
assert entityContent.contains("amount") : "Entity should contain field 'amount'"
assert entityContent.contains("status") : "Entity should contain field 'status'"
assert entityContent.contains("createdAt") : "Entity should contain field 'createdAt'"
assert entityContent.contains("updatedAt") : "Entity should contain field 'updatedAt'"

// 验证类型映射
assert entityContent.contains("Long") : "Entity should contain Long type (for id/userId)"
assert entityContent.contains("String") : "Entity should contain String type (for orderNo)"
assert entityContent.contains("BigDecimal") : "Entity should contain BigDecimal type (for amount)"
assert entityContent.contains("LocalDateTime") : "Entity should contain LocalDateTime type (for createdAt/updatedAt)"

// 验证包声明
assert entityContent.contains("package com.example.entity") : "Entity should have correct package"

println "[basic-ddl] Entity assertions passed."

// ========== 2. 验证 Mapper 接口生成 ==========
File mapperFile = new File(basedir, "src/main/java/com/example/mapper/TOrderMapper.java")
assert mapperFile.exists() : "TOrderMapper.java should be generated"
String mapperContent = mapperFile.text

// 验证是接口
assert mapperContent.contains("interface") : "Mapper file should be an interface"
assert mapperContent.contains("TOrderMapper") : "Mapper should be named TOrderMapper"

// 验证基础CRUD方法
assert mapperContent.contains("insert") : "Mapper should contain insert method"
assert mapperContent.contains("batchInsert") : "Mapper should contain batchInsert method"
assert mapperContent.contains("updateById") : "Mapper should contain updateById method"
assert mapperContent.contains("deleteById") : "Mapper should contain deleteById method"
assert mapperContent.contains("queryById") : "Mapper should contain queryById method"
assert mapperContent.contains("queryByIds") : "Mapper should contain queryByIds method"

// 验证索引方法（uk_order_no -> queryByOrderNo, idx_user_id -> queryByUserId）
assert mapperContent.contains("queryByOrderNo") : "Mapper should contain queryByOrderNo method (from unique index)"
assert mapperContent.contains("queryByUserId") : "Mapper should contain queryByUserId method (from index)"

// 验证包声明
assert mapperContent.contains("package com.example.mapper") : "Mapper should have correct package"

println "[basic-ddl] Mapper assertions passed."

// ========== 3. 验证 Mapper XML 文件生成 ==========
File mapperXmlDir = new File(basedir, "src/main/resources/mapper")
assert mapperXmlDir.exists() : "mapper xml directory should exist"

File mapperXmlFile = new File(mapperXmlDir, "TOrderMapper.xml")
assert mapperXmlFile.exists() : "TOrderMapper.xml should be generated"
String xmlContent = mapperXmlFile.text

// 验证基础结构
assert xmlContent.contains("resultMap") : "XML should contain resultMap"
assert xmlContent.contains("t_order") : "XML should reference table name 't_order'"
assert xmlContent.contains("TOrderMapper") || xmlContent.contains("com.example.mapper.TOrderMapper") : "XML should reference mapper namespace"

// 验证基础SQL方法
assert xmlContent.contains("insert") : "XML should contain insert SQL"
assert xmlContent.contains("updateById") : "XML should contain updateById SQL"
assert xmlContent.contains("deleteById") : "XML should contain deleteById SQL"
assert xmlContent.contains("queryById") : "XML should contain queryById SQL"

// 验证字段映射
assert xmlContent.contains("order_no") : "XML should contain column 'order_no'"
assert xmlContent.contains("user_id") : "XML should contain column 'user_id'"
assert xmlContent.contains("created_at") : "XML should contain column 'created_at'"

println "[basic-ddl] Mapper XML assertions passed."

// ========== 4. 验证 Design 文件生成 ==========
File designDir = new File(basedir, "src/main/java/com/example/design")
assert designDir.exists() : "design directory should exist"

def designFiles = designDir.listFiles()?.findAll { it.name.endsWith(".java") && it.name.contains("TOrder") }
assert designFiles != null && designFiles.size() > 0 : "At least one Design file should be generated for TOrder"

String designContent = designFiles[0].text
assert designContent.contains("package com.example.design") : "Design should have correct package"
assert designContent.contains("class") || designContent.contains("interface") : "Design should contain a type declaration"

println "[basic-ddl] Design assertions passed."

println "[persistence-generator/basic-ddl] All assertions passed."
return true

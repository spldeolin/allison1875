/*
 * persistence-generator multi-table 集成测试验证脚本
 *
 * 验证 DDL 包含多张表时，persistence-generator 正确迭代处理每张表
 */

// ========== 1. 验证 t_user 相关文件 ==========
File userEntity = new File(basedir, "src/main/java/com/example/entity/TUserEntity.java")
assert userEntity.exists() : "TUserEntity.java should be generated"
String userEntityContent = userEntity.text
assert userEntityContent.contains("username") : "TUserEntity should contain field 'username'"
assert userEntityContent.contains("email") : "TUserEntity should contain field 'email'"

File userMapper = new File(basedir, "src/main/java/com/example/mapper/TUserMapper.java")
assert userMapper.exists() : "TUserMapper.java should be generated"
String userMapperContent = userMapper.text
assert userMapperContent.contains("interface") : "TUserMapper should be an interface"
assert userMapperContent.contains("queryByUsername") : "TUserMapper should have queryByUsername (from unique index)"

File userXml = new File(basedir, "src/main/resources/mapper/TUserMapper.xml")
assert userXml.exists() : "TUserMapper.xml should be generated"
String userXmlContent = userXml.text
assert userXmlContent.contains("t_user") : "TUserMapper.xml should reference t_user table"

println "[multi-table] t_user assertions passed."

// ========== 2. 验证 t_product 相关文件 ==========
File productEntity = new File(basedir, "src/main/java/com/example/entity/TProductEntity.java")
assert productEntity.exists() : "TProductEntity.java should be generated"
String productEntityContent = productEntity.text
assert productEntityContent.contains("productName") : "TProductEntity should contain field 'productName'"
assert productEntityContent.contains("BigDecimal") : "TProductEntity should contain BigDecimal type"
assert productEntityContent.contains("Integer") : "TProductEntity should contain Integer type (for stock)"

File productMapper = new File(basedir, "src/main/java/com/example/mapper/TProductMapper.java")
assert productMapper.exists() : "TProductMapper.java should be generated"
String productMapperContent = productMapper.text
assert productMapperContent.contains("queryByCategoryId") : "TProductMapper should have queryByCategoryId"

File productXml = new File(basedir, "src/main/resources/mapper/TProductMapper.xml")
assert productXml.exists() : "TProductMapper.xml should be generated"
String productXmlContent = productXml.text
assert productXmlContent.contains("t_product") : "TProductMapper.xml should reference t_product table"

println "[multi-table] t_product assertions passed."

// ========== 3. 验证 t_order_item 相关文件 ==========
File orderItemEntity = new File(basedir, "src/main/java/com/example/entity/TOrderItemEntity.java")
assert orderItemEntity.exists() : "TOrderItemEntity.java should be generated"
String orderItemEntityContent = orderItemEntity.text
assert orderItemEntityContent.contains("orderId") : "TOrderItemEntity should contain field 'orderId'"
assert orderItemEntityContent.contains("productId") : "TOrderItemEntity should contain field 'productId'"
assert orderItemEntityContent.contains("quantity") : "TOrderItemEntity should contain field 'quantity'"

File orderItemMapper = new File(basedir, "src/main/java/com/example/mapper/TOrderItemMapper.java")
assert orderItemMapper.exists() : "TOrderItemMapper.java should be generated"
String orderItemMapperContent = orderItemMapper.text
assert orderItemMapperContent.contains("queryByOrderId") : "TOrderItemMapper should have queryByOrderId"
assert orderItemMapperContent.contains("queryByProductId") : "TOrderItemMapper should have queryByProductId"

File orderItemXml = new File(basedir, "src/main/resources/mapper/TOrderItemMapper.xml")
assert orderItemXml.exists() : "TOrderItemMapper.xml should be generated"
String orderItemXmlContent = orderItemXml.text
assert orderItemXmlContent.contains("t_order_item") : "TOrderItemMapper.xml should reference t_order_item table"

println "[multi-table] t_order_item assertions passed."

// ========== 4. 验证 Design 文件 ==========
File designDir = new File(basedir, "src/main/java/com/example/design")
assert designDir.exists() : "design directory should exist"
def designFiles = designDir.listFiles()?.findAll { it.name.endsWith(".java") }
// 应该生成3个Design + 1个JoinChain = 4个文件
assert designFiles != null && designFiles.size() >= 3 : "At least 3 Design files should be generated (one per table)"

println "[persistence-generator/multi-table] All assertions passed."
return true

/*
 * persistence-generator no-lombok 集成测试验证脚本
 *
 * 验证 isDataModelWithoutLombok=true 时，Entity 包含手写 getter/setter 而非 Lombok 注解
 */

File entityFile = new File(basedir, "src/main/java/com/example/entity/TItemEntity.java")
assert entityFile.exists() : "TItemEntity.java should be generated"
String entityContent = entityFile.text

// 不应包含 Lombok 注解
assert !entityContent.contains("@Data") : "Entity should NOT have @Data annotation"
assert !entityContent.contains("@Accessors") : "Entity should NOT have @Accessors annotation"
assert !entityContent.contains("@FieldDefaults") : "Entity should NOT have @FieldDefaults annotation"

// 应该有手写的 getter/setter 方法
assert entityContent.contains("getId") || entityContent.contains("getId()") : "Entity should have getId getter"
assert entityContent.contains("setId") : "Entity should have setId setter"
assert entityContent.contains("getItemName") : "Entity should have getItemName getter"
assert entityContent.contains("setItemName") : "Entity should have setItemName setter"
assert entityContent.contains("getQuantity") : "Entity should have getQuantity getter"
assert entityContent.contains("setQuantity") : "Entity should have setQuantity setter"

println "[no-lombok] Entity getter/setter assertions passed."

println "[persistence-generator/no-lombok] All assertions passed."
return true

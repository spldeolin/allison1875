/*
 * no-lombok 集成测试验证脚本
 *
 * 验证 isDataModelWithoutLombok=true 时，生成的 DTO 包含 getter/setter 方法
 * 而非 Lombok 注解（@Data、@Accessors 等）。
 */

File controllerFile = new File(basedir, "src/main/java/com/example/controller/NoLombokController.java")
assert controllerFile.exists() : "NoLombokController.java should exist"
String controllerContent = controllerFile.text

// init 块移除
assert !controllerContent.contains('String handler =') : "init block should be removed"
assert controllerContent.contains("/create-item") : "Should contain URL"

// ========== Req DTO ==========
File reqDtoDir = new File(basedir, "src/main/java/com/example/dto/req")
assert reqDtoDir.exists() : "req DTO directory should exist"
def reqFiles = reqDtoDir.listFiles().findAll { it.name.endsWith(".java") && it.name.contains("CreateItem") && it.name.contains("Req") }
assert reqFiles.size() == 1 : "Should generate 1 Req DTO, found: ${reqFiles*.name}"

String reqContent = reqFiles[0].text
// 应包含 getter/setter 方法
assert reqContent.contains("getItemName") : "Req DTO should contain getter 'getItemName'"
assert reqContent.contains("setItemName") : "Req DTO should contain setter 'setItemName'"
assert reqContent.contains("getQuantity") : "Req DTO should contain getter 'getQuantity'"
assert reqContent.contains("setQuantity") : "Req DTO should contain setter 'setQuantity'"
// 不应包含 Lombok 注解
assert !reqContent.contains("@Data") : "Req DTO should NOT contain @Data (no Lombok)"
assert !reqContent.contains("@Accessors") : "Req DTO should NOT contain @Accessors (no Lombok)"

// ========== Resp DTO ==========
File respDtoDir = new File(basedir, "src/main/java/com/example/dto/resp")
assert respDtoDir.exists() : "resp DTO directory should exist"
def respFiles = respDtoDir.listFiles().findAll { it.name.endsWith(".java") && it.name.contains("CreateItem") && it.name.contains("Resp") }
assert respFiles.size() == 1 : "Should generate 1 Resp DTO, found: ${respFiles*.name}"

String respContent = respFiles[0].text
assert respContent.contains("getItemId") : "Resp DTO should contain getter 'getItemId'"
assert respContent.contains("setItemId") : "Resp DTO should contain setter 'setItemId'"
assert !respContent.contains("@Data") : "Resp DTO should NOT contain @Data (no Lombok)"

// toString, equals, hashCode
assert respContent.contains("toString") : "Resp DTO should contain toString method"
assert respContent.contains("equals") : "Resp DTO should contain equals method"
assert respContent.contains("hashCode") : "Resp DTO should contain hashCode method"

println "[no-lombok] All assertions passed."
return true

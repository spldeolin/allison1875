/*
 * nested-dto 集成测试验证脚本
 *
 * 验证 Req/Resp 内嵌套子类时，子类被提取为独立 DTO 文件，
 * 父类中替换为字段引用，Req 侧嵌套字段带 @Valid。
 */

File controllerFile = new File(basedir, "src/main/java/com/example/controller/ShippingController.java")
assert controllerFile.exists() : "ShippingController.java should exist"
String controllerContent = controllerFile.text

// init 块移除
assert !controllerContent.contains('class Req') : "inner class Req should be removed"
assert !controllerContent.contains('class Resp') : "inner class Resp should be removed"
assert !controllerContent.contains('class Address') : "nested class Address should be removed"
assert !controllerContent.contains('class Logistics') : "nested class Logistics should be removed"

// handler 生成
assert controllerContent.contains("/create-shipping") : "Should contain URL"
assert controllerContent.contains("createShipping") : "Should contain method name"

// 验证嵌套 DTO 独立文件生成
File reqDtoDir = new File(basedir, "src/main/java/com/example/dto/req")
assert reqDtoDir.exists() : "req DTO directory should exist"

// Address 嵌套类应生成独立 DTO
def addressFiles = reqDtoDir.listFiles().findAll { it.name.endsWith(".java") && it.name.contains("Address") }
assert addressFiles.size() == 1 : "Should generate AddressDTO file in req package, found: ${addressFiles*.name}"

String addressContent = addressFiles[0].text
assert addressContent.contains("province") : "AddressDTO should contain 'province'"
assert addressContent.contains("city") : "AddressDTO should contain 'city'"
assert addressContent.contains("detail") : "AddressDTO should contain 'detail'"

// Req 主文件
def reqFiles = reqDtoDir.listFiles().findAll { it.name.endsWith(".java") && it.name.contains("CreateShipping") && it.name.contains("Req") }
assert reqFiles.size() == 1 : "Should generate CreateShippingReq DTO, found: ${reqFiles*.name}"

String reqContent = reqFiles[0].text
assert reqContent.contains("recipientName") : "Req should contain 'recipientName'"
// 嵌套类应被替换为字段引用（address 字段）
assert reqContent.contains("address") || reqContent.contains("Address") : "Req should reference Address as a field"
// Req 侧嵌套字段应有 @Valid
assert reqContent.contains("Valid") : "Nested DTO field in Req should have @Valid annotation"

// Resp 嵌套 DTO
File respDtoDir = new File(basedir, "src/main/java/com/example/dto/resp")
assert respDtoDir.exists() : "resp DTO directory should exist"

def logisticsFiles = respDtoDir.listFiles().findAll { it.name.endsWith(".java") && it.name.contains("Logistics") }
assert logisticsFiles.size() == 1 : "Should generate LogisticsDTO file in resp package, found: ${logisticsFiles*.name}"

String logisticsContent = logisticsFiles[0].text
assert logisticsContent.contains("company") : "LogisticsDTO should contain 'company'"
assert logisticsContent.contains("trackingNo") : "LogisticsDTO should contain 'trackingNo'"

println "[nested-dto] All assertions passed."
return true

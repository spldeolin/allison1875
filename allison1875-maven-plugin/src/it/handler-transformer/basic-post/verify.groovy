/*
 * basic-post 集成测试验证脚本
 *
 * 验证 handler-transformer 将 Controller 中的 init 块转换为 @PostMapping handler 方法，
 * 并生成 ReqDTO、RespDTO、Service 接口及 ServiceImpl 文件。
 */

// ========== 1. 验证 Controller 被改写 ==========
File controllerFile = new File(basedir, "src/main/java/com/example/controller/OrderController.java")
assert controllerFile.exists() : "OrderController.java should exist"
String controllerContent = controllerFile.text

// init 块应该被移除
assert !controllerContent.contains('String handler = "/create-order"') : "init block variable 'handler' should be removed"
assert !controllerContent.contains('String desc = "创建订单"') : "init block variable 'desc' should be removed"
assert !controllerContent.contains('class Req') : "inner class Req should be removed from controller"
assert !controllerContent.contains('class Resp') : "inner class Resp should be removed from controller"

// 应生成 @PostMapping handler 方法
assert controllerContent.contains("PostMapping") : "Controller should contain @PostMapping annotation"
assert controllerContent.contains("/create-order") : "Controller should contain handler URL '/create-order'"
assert controllerContent.contains("createOrder") : "Controller should contain method named 'createOrder'"

// 应注入 Service
assert controllerContent.contains("Autowired") || controllerContent.contains("@Inject") : "Controller should have injected service field"

// ========== 2. 验证 Req DTO 文件生成 ==========
File reqDtoDir = new File(basedir, "src/main/java/com/example/dto/req")
assert reqDtoDir.exists() : "req DTO directory should exist"

def reqFiles = reqDtoDir.listFiles().findAll { it.name.endsWith(".java") && it.name.contains("CreateOrder") && it.name.contains("Req") }
assert reqFiles.size() == 1 : "Should generate exactly 1 Req DTO file for CreateOrder, found: ${reqFiles*.name}"

String reqContent = reqFiles[0].text
assert reqContent.contains("orderName") : "Req DTO should contain field 'orderName'"
assert reqContent.contains("amount") : "Req DTO should contain field 'amount'"
assert reqContent.contains("NotBlank") : "Req DTO should preserve @NotBlank annotation"
assert reqContent.contains("NotNull") : "Req DTO should preserve @NotNull annotation"

// ========== 3. 验证 Resp DTO 文件生成 ==========
File respDtoDir = new File(basedir, "src/main/java/com/example/dto/resp")
assert respDtoDir.exists() : "resp DTO directory should exist"

def respFiles = respDtoDir.listFiles().findAll { it.name.endsWith(".java") && it.name.contains("CreateOrder") && it.name.contains("Resp") }
assert respFiles.size() == 1 : "Should generate exactly 1 Resp DTO file for CreateOrder, found: ${respFiles*.name}"

String respContent = respFiles[0].text
assert respContent.contains("orderId") : "Resp DTO should contain field 'orderId'"
assert respContent.contains("status") : "Resp DTO should contain field 'status'"

// ========== 4. 验证 Service 接口生成 ==========
File serviceDir = new File(basedir, "src/main/java/com/example/service")
assert serviceDir.exists() : "service directory should exist"

def serviceFiles = serviceDir.listFiles().findAll { it.name.endsWith(".java") && it.name.contains("CreateOrder") && it.name.contains("Service") }
assert serviceFiles.size() == 1 : "Should generate exactly 1 Service interface file, found: ${serviceFiles*.name}"

String serviceContent = serviceFiles[0].text
assert serviceContent.contains("interface") : "Service file should be an interface"
assert serviceContent.contains("createOrder") : "Service should contain method 'createOrder'"

// ========== 5. 验证 ServiceImpl 生成 ==========
File serviceImplDir = new File(basedir, "src/main/java/com/example/service/impl")
assert serviceImplDir.exists() : "service/impl directory should exist"

def serviceImplFiles = serviceImplDir.listFiles().findAll { it.name.endsWith(".java") && it.name.contains("CreateOrder") && it.name.contains("Impl") }
assert serviceImplFiles.size() == 1 : "Should generate exactly 1 ServiceImpl file, found: ${serviceImplFiles*.name}"

String serviceImplContent = serviceImplFiles[0].text
assert serviceImplContent.contains("class") : "ServiceImpl file should contain a class"
assert serviceImplContent.contains("createOrder") : "ServiceImpl should contain method 'createOrder'"
assert serviceImplContent.contains("implements") : "ServiceImpl should implement the service interface"

println "[basic-post] All assertions passed."
return true

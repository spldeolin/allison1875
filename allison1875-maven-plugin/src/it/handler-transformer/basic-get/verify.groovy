/*
 * basic-get 集成测试验证脚本
 *
 * 验证 handler-transformer 对 @GetUrlQuery 标注的 Req 类生成 @GetMapping handler，
 * Req 字段作为 query params 而非 @RequestBody。
 */

// ========== 1. 验证 Controller 被改写 ==========
File controllerFile = new File(basedir, "src/main/java/com/example/controller/UserController.java")
assert controllerFile.exists() : "UserController.java should exist"
String controllerContent = controllerFile.text

// init 块应该被移除
assert !controllerContent.contains('String handler = "/list-users"') : "init block variable 'handler' should be removed"
assert !controllerContent.contains('class Req') : "inner class Req should be removed from controller"

// 应生成 @GetMapping handler 方法（因为 Req 标注了 @GetUrlQuery）
assert controllerContent.contains("GetMapping") : "Controller should contain @GetMapping annotation"
assert controllerContent.contains("/list-users") : "Controller should contain handler URL '/list-users'"
assert controllerContent.contains("listUsers") : "Controller should contain method named 'listUsers'"

// GET 请求不应该有 @RequestBody
assert !controllerContent.contains("@RequestBody") : "GET handler should not have @RequestBody"

// 应有 query param 参数（keyword, pageNo）
assert controllerContent.contains("keyword") : "Handler should have query param 'keyword'"
assert controllerContent.contains("pageNo") : "Handler should have query param 'pageNo'"

// ========== 2. 验证 Resp DTO 文件生成 ==========
File respDtoDir = new File(basedir, "src/main/java/com/example/dto/resp")
assert respDtoDir.exists() : "resp DTO directory should exist"

def respFiles = respDtoDir.listFiles().findAll { it.name.endsWith(".java") && it.name.contains("ListUsers") && it.name.contains("Resp") }
assert respFiles.size() == 1 : "Should generate exactly 1 Resp DTO file for ListUsers, found: ${respFiles*.name}"

String respContent = respFiles[0].text
assert respContent.contains("userId") : "Resp DTO should contain field 'userId'"
assert respContent.contains("username") : "Resp DTO should contain field 'username'"

// ========== 3. @GetUrlQuery 的 Req 不应生成独立 DTO 文件 ==========
File reqDtoDir = new File(basedir, "src/main/java/com/example/dto/req")
if (reqDtoDir.exists()) {
    def reqFiles = reqDtoDir.listFiles().findAll { it.name.endsWith(".java") && it.name.contains("ListUsers") }
    assert reqFiles.size() == 0 : "GET handler with @GetUrlQuery should NOT generate a Req DTO file, found: ${reqFiles*.name}"
}

// ========== 4. 验证 Service 接口生成 ==========
File serviceDir = new File(basedir, "src/main/java/com/example/service")
assert serviceDir.exists() : "service directory should exist"

def serviceFiles = serviceDir.listFiles().findAll { it.name.endsWith(".java") && it.name.contains("ListUsers") }
assert serviceFiles.size() == 1 : "Should generate exactly 1 Service interface file, found: ${serviceFiles*.name}"

println "[basic-get] All assertions passed."
return true

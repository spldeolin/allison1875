/*
 * no-req-no-resp 集成测试验证脚本
 *
 * 验证 init 块只包含 handler+desc、无 Req/Resp 时，
 * 生成 void 返回、无参数的 PostMapping handler 和 Service。
 */

File controllerFile = new File(basedir, "src/main/java/com/example/controller/HealthController.java")
assert controllerFile.exists() : "HealthController.java should exist"
String controllerContent = controllerFile.text

// init 块被移除
assert !controllerContent.contains('String handler = "/ping"') : "init block should be removed"

// 生成 handler 方法
assert controllerContent.contains("PostMapping") || controllerContent.contains("GetMapping") : "Controller should contain mapping annotation"
assert controllerContent.contains("/ping") : "Controller should contain handler URL '/ping'"
assert controllerContent.contains("ping") : "Controller should contain method named 'ping'"

// 无 @RequestBody（没有 Req）
assert !controllerContent.contains("@RequestBody") : "Handler without Req should not have @RequestBody"

// 验证 Service 生成
File serviceDir = new File(basedir, "src/main/java/com/example/service")
assert serviceDir.exists() : "service directory should exist"

def serviceFiles = serviceDir.listFiles().findAll { it.name.endsWith(".java") && it.name.contains("Ping") }
assert serviceFiles.size() == 1 : "Should generate exactly 1 Service file for Ping, found: ${serviceFiles*.name}"

String serviceContent = serviceFiles[0].text
assert serviceContent.contains("void") : "Service method should return void (no Resp defined)"

// 不应生成任何 DTO 文件
File reqDtoDir = new File(basedir, "src/main/java/com/example/dto/req")
File respDtoDir = new File(basedir, "src/main/java/com/example/dto/resp")
if (reqDtoDir.exists()) {
    assert reqDtoDir.listFiles().findAll { it.name.endsWith(".java") }.size() == 0 : "No Req DTO should be generated"
}
if (respDtoDir.exists()) {
    assert respDtoDir.listFiles().findAll { it.name.endsWith(".java") }.size() == 0 : "No Resp DTO should be generated"
}

println "[no-req-no-resp] All assertions passed."
return true

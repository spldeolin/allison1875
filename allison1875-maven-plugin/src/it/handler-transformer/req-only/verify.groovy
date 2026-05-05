/*
 * req-only 集成测试验证脚本
 *
 * 验证 init 块只有 Req、没有 Resp 时，生成有 @RequestBody 参数但 void 返回的 handler。
 */

File controllerFile = new File(basedir, "src/main/java/com/example/controller/NotificationController.java")
assert controllerFile.exists() : "NotificationController.java should exist"
String controllerContent = controllerFile.text

// init 块移除
assert !controllerContent.contains('String handler = "/send-notification"') : "init block should be removed"
assert !controllerContent.contains('class Req') : "inner class Req should be removed"

// handler 方法生成
assert controllerContent.contains("PostMapping") : "Controller should contain @PostMapping"
assert controllerContent.contains("/send-notification") : "Controller should contain URL '/send-notification'"
assert controllerContent.contains("sendNotification") : "Controller should contain method 'sendNotification'"

// 有 @RequestBody
assert controllerContent.contains("RequestBody") : "Handler with Req should have @RequestBody"

// handler 方法返回 void
assert controllerContent.contains("void") : "Handler without Resp should return void"

// 验证 Req DTO 生成
File reqDtoDir = new File(basedir, "src/main/java/com/example/dto/req")
assert reqDtoDir.exists() : "req DTO directory should exist"

def reqFiles = reqDtoDir.listFiles().findAll { it.name.endsWith(".java") && it.name.contains("SendNotification") && it.name.contains("Req") }
assert reqFiles.size() == 1 : "Should generate 1 Req DTO file, found: ${reqFiles*.name}"

String reqContent = reqFiles[0].text
assert reqContent.contains("receiverId") : "Req DTO should contain 'receiverId'"
assert reqContent.contains("content") : "Req DTO should contain 'content'"
assert reqContent.contains("NotNull") : "Req DTO should preserve @NotNull"
assert reqContent.contains("NotBlank") : "Req DTO should preserve @NotBlank"

// 不应生成 Resp DTO
File respDtoDir = new File(basedir, "src/main/java/com/example/dto/resp")
if (respDtoDir.exists()) {
    assert respDtoDir.listFiles().findAll { it.name.endsWith(".java") }.size() == 0 : "No Resp DTO should be generated"
}

println "[req-only] All assertions passed."
return true

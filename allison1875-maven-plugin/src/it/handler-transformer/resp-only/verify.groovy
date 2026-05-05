/*
 * resp-only 集成测试验证脚本
 *
 * 验证 init 块只有 Resp、没有 Req 时，生成无参数但有返回值的 handler。
 */

File controllerFile = new File(basedir, "src/main/java/com/example/controller/ConfigController.java")
assert controllerFile.exists() : "ConfigController.java should exist"
String controllerContent = controllerFile.text

// init 块移除
assert !controllerContent.contains('String handler = "/get-config"') : "init block should be removed"
assert !controllerContent.contains('class Resp') : "inner class Resp should be removed"

// handler 方法生成
assert controllerContent.contains("/get-config") : "Controller should contain URL '/get-config'"
assert controllerContent.contains("getConfig") : "Controller should contain method 'getConfig'"

// 无 @RequestBody
assert !controllerContent.contains("@RequestBody") : "Handler without Req should not have @RequestBody"

// 验证 Resp DTO 生成
File respDtoDir = new File(basedir, "src/main/java/com/example/dto/resp")
assert respDtoDir.exists() : "resp DTO directory should exist"

def respFiles = respDtoDir.listFiles().findAll { it.name.endsWith(".java") && it.name.contains("GetConfig") && it.name.contains("Resp") }
assert respFiles.size() == 1 : "Should generate 1 Resp DTO file, found: ${respFiles*.name}"

String respContent = respFiles[0].text
assert respContent.contains("appName") : "Resp DTO should contain 'appName'"
assert respContent.contains("version") : "Resp DTO should contain 'version'"
assert respContent.contains("maintenance") : "Resp DTO should contain 'maintenance'"

// 不应生成 Req DTO
File reqDtoDir = new File(basedir, "src/main/java/com/example/dto/req")
if (reqDtoDir.exists()) {
    assert reqDtoDir.listFiles().findAll { it.name.endsWith(".java") }.size() == 0 : "No Req DTO should be generated"
}

println "[resp-only] All assertions passed."
return true

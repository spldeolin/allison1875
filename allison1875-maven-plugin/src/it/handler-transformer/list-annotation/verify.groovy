/*
 * list-annotation 集成测试验证脚本
 *
 * 验证 Resp 标注 @L 注解时，handler 返回类型为 List<XxxResp>。
 */

File controllerFile = new File(basedir, "src/main/java/com/example/controller/TagController.java")
assert controllerFile.exists() : "TagController.java should exist"
String controllerContent = controllerFile.text

// init 块移除
assert !controllerContent.contains('class Resp') : "inner class Resp should be removed"

// handler 生成
assert controllerContent.contains("/list-tags") : "Should contain URL '/list-tags'"
assert controllerContent.contains("listTags") : "Should contain method 'listTags'"

// 返回类型应该是 List（因为 @L 注解）
assert controllerContent.contains("List") : "Handler return type should contain 'List' due to @L annotation"

// Resp DTO 生成
File respDtoDir = new File(basedir, "src/main/java/com/example/dto/resp")
assert respDtoDir.exists() : "resp DTO directory should exist"

def respFiles = respDtoDir.listFiles().findAll { it.name.endsWith(".java") && it.name.contains("ListTags") && it.name.contains("Resp") }
assert respFiles.size() == 1 : "Should generate 1 Resp DTO file, found: ${respFiles*.name}"

String respContent = respFiles[0].text
assert respContent.contains("tagId") : "Resp DTO should contain 'tagId'"
assert respContent.contains("tagName") : "Resp DTO should contain 'tagName'"

// Service 方法返回类型也应包含 List
File serviceDir = new File(basedir, "src/main/java/com/example/service")
assert serviceDir.exists() : "service directory should exist"
def serviceFiles = serviceDir.listFiles().findAll { it.name.endsWith(".java") && it.name.contains("ListTags") }
assert serviceFiles.size() == 1 : "Should generate 1 Service file, found: ${serviceFiles*.name}"
String serviceContent = serviceFiles[0].text
assert serviceContent.contains("List") : "Service method return type should contain 'List'"

println "[list-annotation] All assertions passed."
return true

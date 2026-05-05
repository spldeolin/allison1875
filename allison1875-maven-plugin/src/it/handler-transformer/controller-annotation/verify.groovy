/*
 * controller-annotation 集成测试验证脚本
 *
 * 验证使用 @Controller（而非 @RestController）标注的类也能被检测并转换。
 */

File controllerFile = new File(basedir, "src/main/java/com/example/controller/LegacyController.java")
assert controllerFile.exists() : "LegacyController.java should exist"
String controllerContent = controllerFile.text

// init 块移除
assert !controllerContent.contains('String handler = "/do-legacy"') : "init block should be removed"
assert !controllerContent.contains('class Req') : "inner class Req should be removed"

// handler 方法生成
assert controllerContent.contains("/do-legacy") : "Should contain URL '/do-legacy'"
assert controllerContent.contains("doLegacy") : "Should contain method 'doLegacy'"
assert controllerContent.contains("PostMapping") : "Should contain @PostMapping"

// @Controller 注解保留
assert controllerContent.contains("@Controller") : "@Controller annotation should be preserved"

// Service 正常生成
File serviceDir = new File(basedir, "src/main/java/com/example/service")
assert serviceDir.exists() : "service directory should exist"
def serviceFiles = serviceDir.listFiles().findAll { it.name.endsWith(".java") && it.name.contains("DoLegacy") }
assert serviceFiles.size() == 1 : "Should generate 1 Service file, found: ${serviceFiles*.name}"

// DTO 正常生成
File reqDtoDir = new File(basedir, "src/main/java/com/example/dto/req")
assert reqDtoDir.exists() : "req DTO directory should exist"
def reqFiles = reqDtoDir.listFiles().findAll { it.name.endsWith(".java") && it.name.contains("DoLegacy") }
assert reqFiles.size() == 1 : "Should generate 1 Req DTO, found: ${reqFiles*.name}"

println "[controller-annotation] All assertions passed."
return true

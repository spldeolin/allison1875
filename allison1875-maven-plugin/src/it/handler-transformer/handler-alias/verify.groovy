/*
 * handler-alias 集成测试验证脚本
 *
 * 验证 init 块使用 h/d 别名代替 handler/desc 时，工具仍能正确解析。
 */

File controllerFile = new File(basedir, "src/main/java/com/example/controller/AliasController.java")
assert controllerFile.exists() : "AliasController.java should exist"
String controllerContent = controllerFile.text

// init 块移除（h/d 变量声明应被清除）
assert !controllerContent.contains('String h = "/do-something"') : "init block alias 'h' should be removed"
assert !controllerContent.contains('String d = "执行操作"') : "init block alias 'd' should be removed"
assert !controllerContent.contains('class Req') : "inner class Req should be removed"

// handler 方法生成
assert controllerContent.contains("/do-something") : "Should contain URL '/do-something'"
assert controllerContent.contains("doSomething") : "Should contain method 'doSomething'"
assert controllerContent.contains("PostMapping") : "Should contain @PostMapping"

// desc 应该出现在 javadoc 中
assert controllerContent.contains("执行操作") : "Handler javadoc should contain desc '执行操作'"

// Service 正常生成
File serviceDir = new File(basedir, "src/main/java/com/example/service")
assert serviceDir.exists() : "service directory should exist"
def serviceFiles = serviceDir.listFiles().findAll { it.name.endsWith(".java") && it.name.contains("DoSomething") }
assert serviceFiles.size() == 1 : "Should generate 1 Service file, found: ${serviceFiles*.name}"

println "[handler-alias] All assertions passed."
return true

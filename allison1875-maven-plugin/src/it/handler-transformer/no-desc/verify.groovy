/*
 * no-desc 集成测试验证脚本
 *
 * 验证 init 块只有 handler 无 desc 时，工具使用默认描述完成转换而非报错。
 */

File controllerFile = new File(basedir, "src/main/java/com/example/controller/LogController.java")
assert controllerFile.exists() : "LogController.java should exist"
String controllerContent = controllerFile.text

// init 块被替换
assert !controllerContent.contains('String handler = "/clear-log"') : "init block should be removed"

// handler 方法生成
assert controllerContent.contains("/clear-log") : "Should contain URL '/clear-log'"
assert controllerContent.contains("clearLog") : "Should contain method 'clearLog'"

// 默认描述 "未指定描述" 应出现在 handler 的 Javadoc 中
assert controllerContent.contains("未指定描述") : "Handler javadoc should contain default description '未指定描述'"

// Service 正常生成
File serviceDir = new File(basedir, "src/main/java/com/example/service")
assert serviceDir.exists() : "service directory should exist"
def serviceFiles = serviceDir.listFiles().findAll { it.name.endsWith(".java") && it.name.contains("ClearLog") }
assert serviceFiles.size() == 1 : "Should generate 1 Service file, found: ${serviceFiles*.name}"

println "[no-desc] All assertions passed."
return true

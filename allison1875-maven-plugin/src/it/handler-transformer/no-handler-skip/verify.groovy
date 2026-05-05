/*
 * no-handler-skip 集成测试验证脚本
 *
 * 验证 init 块不包含 handler 变量时，该 init 块被跳过，Controller 保持不变。
 */

File controllerFile = new File(basedir, "src/main/java/com/example/controller/SkipController.java")
assert controllerFile.exists() : "SkipController.java should exist"
String controllerContent = controllerFile.text

// init 块应该保持不变（因为没有 handler 变量，工具应跳过）
assert controllerContent.contains('String desc = "这个 init 块缺少 handler 变量"') : "init block should remain unchanged (no handler → skip)"
assert controllerContent.contains('class Req') : "inner class Req should remain (init block not processed)"

// 不应生成任何 handler 方法
assert !controllerContent.contains("PostMapping") : "No handler method should be generated"
assert !controllerContent.contains("GetMapping") : "No handler method should be generated"

// 不应生成 Service 文件
File serviceDir = new File(basedir, "src/main/java/com/example/service")
if (serviceDir.exists()) {
    def serviceFiles = serviceDir.listFiles().findAll { it.name.endsWith(".java") }
    assert serviceFiles.size() == 0 : "No Service files should be generated when handler is skipped, found: ${serviceFiles*.name}"
}

// 不应生成 DTO 文件
File reqDtoDir = new File(basedir, "src/main/java/com/example/dto/req")
if (reqDtoDir.exists()) {
    assert reqDtoDir.listFiles().findAll { it.name.endsWith(".java") }.size() == 0 : "No DTO files should be generated"
}

println "[no-handler-skip] All assertions passed."
return true

/*
 * enable-one-service 集成测试验证脚本
 *
 * 验证 enableOneService=true 时，同一 Controller 的多个 init 块共享一个 Service 接口+Impl，
 * 多个方法聚合在同一个文件中。
 */

File controllerFile = new File(basedir, "src/main/java/com/example/controller/AccountController.java")
assert controllerFile.exists() : "AccountController.java should exist"
String controllerContent = controllerFile.text

// 所有 init 块被替换
assert !controllerContent.contains('String handler =') : "All init blocks should be removed"
assert controllerContent.contains("/create-account") : "Should contain URL '/create-account'"
assert controllerContent.contains("/freeze-account") : "Should contain URL '/freeze-account'"
assert controllerContent.contains("/get-account-balance") : "Should contain URL '/get-account-balance'"

// oneService 模式下只应生成 1 个 Service 接口（名称基于 Controller：AccountService）
File serviceDir = new File(basedir, "src/main/java/com/example/service")
assert serviceDir.exists() : "service directory should exist"

def serviceFiles = serviceDir.listFiles().findAll { it.name.endsWith(".java") }
assert serviceFiles.size() == 1 : "enableOneService=true should generate exactly 1 Service interface, found: ${serviceFiles*.name}"
assert serviceFiles[0].name.contains("AccountService") : "Service name should be 'AccountService' (derived from AccountController), found: ${serviceFiles[0].name}"

// 该 Service 接口应包含 3 个方法
String serviceContent = serviceFiles[0].text
assert serviceContent.contains("createAccount") : "Service should contain 'createAccount'"
assert serviceContent.contains("freezeAccount") : "Service should contain 'freezeAccount'"
assert serviceContent.contains("getAccountBalance") : "Service should contain 'getAccountBalance'"

// 只应生成 1 个 ServiceImpl
File serviceImplDir = new File(basedir, "src/main/java/com/example/service/impl")
assert serviceImplDir.exists() : "service/impl directory should exist"

def serviceImplFiles = serviceImplDir.listFiles().findAll { it.name.endsWith(".java") }
assert serviceImplFiles.size() == 1 : "enableOneService=true should generate exactly 1 ServiceImpl, found: ${serviceImplFiles*.name}"

String serviceImplContent = serviceImplFiles[0].text
assert serviceImplContent.contains("createAccount") : "ServiceImpl should contain 'createAccount'"
assert serviceImplContent.contains("freezeAccount") : "ServiceImpl should contain 'freezeAccount'"
assert serviceImplContent.contains("getAccountBalance") : "ServiceImpl should contain 'getAccountBalance'"

println "[enable-one-service] All assertions passed."
return true

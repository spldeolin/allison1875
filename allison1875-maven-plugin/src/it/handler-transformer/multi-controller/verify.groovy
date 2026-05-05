/*
 * multi-controller 集成测试验证脚本
 *
 * 验证项目包含多个 Controller 文件时，每个都被独立检测和转换。
 */

// ========== First Controller ==========
File firstFile = new File(basedir, "src/main/java/com/example/controller/FirstController.java")
assert firstFile.exists() : "FirstController.java should exist"
String firstContent = firstFile.text

assert !firstContent.contains('String handler =') : "First init block should be removed"
assert firstContent.contains("/do-first") : "First should contain URL '/do-first'"
assert firstContent.contains("doFirst") : "First should contain method 'doFirst'"
assert firstContent.contains("PostMapping") : "First should have @PostMapping"

// ========== Second Controller ==========
File secondFile = new File(basedir, "src/main/java/com/example/controller/SecondController.java")
assert secondFile.exists() : "SecondController.java should exist"
String secondContent = secondFile.text

assert !secondContent.contains('String handler =') : "Second init block should be removed"
assert secondContent.contains("/do-second") : "Second should contain URL '/do-second'"
assert secondContent.contains("doSecond") : "Second should contain method 'doSecond'"
assert secondContent.contains("PostMapping") : "Second should have @PostMapping"

// ========== 各自生成独立 Service ==========
File serviceDir = new File(basedir, "src/main/java/com/example/service")
assert serviceDir.exists() : "service directory should exist"

def serviceFiles = serviceDir.listFiles().findAll { it.name.endsWith(".java") }
def firstService = serviceFiles.findAll { it.name.contains("DoFirst") }
def secondService = serviceFiles.findAll { it.name.contains("DoSecond") }
assert firstService.size() == 1 : "Should generate Service for DoFirst, found: ${firstService*.name}"
assert secondService.size() == 1 : "Should generate Service for DoSecond, found: ${secondService*.name}"

// ========== 各自生成 DTO ==========
File reqDtoDir = new File(basedir, "src/main/java/com/example/dto/req")
assert reqDtoDir.exists() : "req DTO directory should exist"
def reqFiles = reqDtoDir.listFiles().findAll { it.name.endsWith(".java") }
assert reqFiles.size() >= 2 : "Should generate at least 2 Req DTOs (one per controller), found: ${reqFiles*.name}"

File respDtoDir = new File(basedir, "src/main/java/com/example/dto/resp")
assert respDtoDir.exists() : "resp DTO directory should exist"
def respFiles = respDtoDir.listFiles().findAll { it.name.endsWith(".java") }
assert respFiles.size() >= 2 : "Should generate at least 2 Resp DTOs (one per controller), found: ${respFiles*.name}"

println "[multi-controller] All assertions passed."
return true

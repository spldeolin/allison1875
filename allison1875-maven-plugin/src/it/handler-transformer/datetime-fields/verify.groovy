/*
 * datetime-fields 集成测试验证脚本
 *
 * 验证 Req/Resp 中的 Date、LocalDateTime、LocalDate、LocalTime 字段
 * 自动附加 @JsonFormat 注解。
 */

File controllerFile = new File(basedir, "src/main/java/com/example/controller/EventController.java")
assert controllerFile.exists() : "EventController.java should exist"
String controllerContent = controllerFile.text

// init 块移除
assert !controllerContent.contains('String handler =') : "init block should be removed"
assert controllerContent.contains("/create-event") : "Should contain URL"
assert controllerContent.contains("createEvent") : "Should contain method name"

// ========== Req DTO ==========
File reqDtoDir = new File(basedir, "src/main/java/com/example/dto/req")
assert reqDtoDir.exists() : "req DTO directory should exist"
def reqFiles = reqDtoDir.listFiles().findAll { it.name.endsWith(".java") && it.name.contains("CreateEvent") && it.name.contains("Req") }
assert reqFiles.size() == 1 : "Should generate 1 Req DTO, found: ${reqFiles*.name}"

String reqContent = reqFiles[0].text
// Date / LocalDateTime 字段应有 @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
assert reqContent.contains("JsonFormat") : "Req DTO should contain @JsonFormat annotation"
assert reqContent.contains("yyyy-MM-dd HH:mm:ss") : "Date/LocalDateTime field should have pattern 'yyyy-MM-dd HH:mm:ss'"
// LocalDate 字段应有 @JsonFormat(pattern = "yyyy-MM-dd")
assert reqContent.contains("yyyy-MM-dd\"") || reqContent.contains('yyyy-MM-dd"') : "LocalDate field should have pattern 'yyyy-MM-dd'"
// LocalTime 字段应有 @JsonFormat(pattern = "HH:mm:ss")
assert reqContent.contains("HH:mm:ss\"") || reqContent.contains('HH:mm:ss"') : "LocalTime field should have pattern 'HH:mm:ss'"

// ========== Resp DTO ==========
File respDtoDir = new File(basedir, "src/main/java/com/example/dto/resp")
assert respDtoDir.exists() : "resp DTO directory should exist"
def respFiles = respDtoDir.listFiles().findAll { it.name.endsWith(".java") && it.name.contains("CreateEvent") && it.name.contains("Resp") }
assert respFiles.size() == 1 : "Should generate 1 Resp DTO, found: ${respFiles*.name}"

String respContent = respFiles[0].text
assert respContent.contains("JsonFormat") : "Resp DTO should also contain @JsonFormat for Date/LocalDateTime fields"

// ========== Req DTO 还应包含 Long 字段的 @JsonSerialize ==========
// Req 中无 Long 字段，但 Resp 中有 eventId (Long)。Req 侧 Long 才会加 @JsonSerialize。
// 本 case 主要验证 @JsonFormat 路径。

println "[datetime-fields] All assertions passed."
return true

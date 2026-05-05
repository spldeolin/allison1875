/*
 * page-annotation 集成测试验证脚本
 *
 * 验证 Resp 标注 @P 注解时，handler 返回类型被包装为 PageResult<XxxResp>。
 */

File controllerFile = new File(basedir, "src/main/java/com/example/controller/ArticleController.java")
assert controllerFile.exists() : "ArticleController.java should exist"
String controllerContent = controllerFile.text

// init 块移除
assert !controllerContent.contains('String handler =') : "init block should be removed"

// handler 方法生成
assert controllerContent.contains("/page-articles") : "Should contain URL '/page-articles'"
assert controllerContent.contains("pageArticles") : "Should contain method 'pageArticles'"

// 返回类型应包含 PageResult（由 @P 注解触发）
assert controllerContent.contains("PageResult") : "Handler return type should contain 'PageResult' due to @P annotation"

// Service 方法返回类型也应包含 PageResult
File serviceDir = new File(basedir, "src/main/java/com/example/service")
assert serviceDir.exists() : "service directory should exist"
def serviceFiles = serviceDir.listFiles().findAll { it.name.endsWith(".java") && it.name.contains("PageArticles") }
assert serviceFiles.size() == 1 : "Should generate 1 Service file, found: ${serviceFiles*.name}"
String serviceContent = serviceFiles[0].text
assert serviceContent.contains("PageResult") : "Service method return type should contain 'PageResult'"

// Resp DTO 生成
File respDtoDir = new File(basedir, "src/main/java/com/example/dto/resp")
assert respDtoDir.exists() : "resp DTO directory should exist"
def respFiles = respDtoDir.listFiles().findAll { it.name.endsWith(".java") && it.name.contains("PageArticles") && it.name.contains("Resp") }
assert respFiles.size() == 1 : "Should generate 1 Resp DTO file, found: ${respFiles*.name}"

String respContent = respFiles[0].text
assert respContent.contains("articleId") : "Resp DTO should contain 'articleId'"
assert respContent.contains("title") : "Resp DTO should contain 'title'"

println "[page-annotation] All assertions passed."
return true

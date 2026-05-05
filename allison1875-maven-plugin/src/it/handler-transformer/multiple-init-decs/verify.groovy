/*
 * multiple-init-decs 集成测试验证脚本
 *
 * 验证同一个 Controller 包含多个 init 块时，每个都被独立转换。
 */

File controllerFile = new File(basedir, "src/main/java/com/example/controller/ProductController.java")
assert controllerFile.exists() : "ProductController.java should exist"
String controllerContent = controllerFile.text

// 所有 init 块应被移除
assert !controllerContent.contains('String handler =') : "All init block handler declarations should be removed"
assert !controllerContent.contains('String desc =') : "All init block desc declarations should be removed"

// 3 个 handler 方法应该全部生成
assert controllerContent.contains("/create-product") : "Should contain URL '/create-product'"
assert controllerContent.contains("/delete-product") : "Should contain URL '/delete-product'"
assert controllerContent.contains("/get-product-detail") : "Should contain URL '/get-product-detail'"
assert controllerContent.contains("createProduct") : "Should contain method 'createProduct'"
assert controllerContent.contains("deleteProduct") : "Should contain method 'deleteProduct'"
assert controllerContent.contains("getProductDetail") : "Should contain method 'getProductDetail'"

// 应注入多个 Service（非 oneService 模式下每个 init 块生成独立 Service）
// 至少验证 Service 目录下有 3 个文件
File serviceDir = new File(basedir, "src/main/java/com/example/service")
assert serviceDir.exists() : "service directory should exist"
def serviceFiles = serviceDir.listFiles().findAll { it.name.endsWith(".java") }
assert serviceFiles.size() >= 3 : "Should generate at least 3 Service interface files (one per init block), found: ${serviceFiles*.name}"

// 验证 ServiceImpl 目录
File serviceImplDir = new File(basedir, "src/main/java/com/example/service/impl")
assert serviceImplDir.exists() : "service/impl directory should exist"
def serviceImplFiles = serviceImplDir.listFiles().findAll { it.name.endsWith(".java") }
assert serviceImplFiles.size() >= 3 : "Should generate at least 3 ServiceImpl files, found: ${serviceImplFiles*.name}"

// 验证各 DTO 生成
File reqDtoDir = new File(basedir, "src/main/java/com/example/dto/req")
assert reqDtoDir.exists() : "req DTO directory should exist"
def reqFiles = reqDtoDir.listFiles().findAll { it.name.endsWith(".java") }
// create-product 和 delete-product 各有一个 Req
assert reqFiles.size() >= 2 : "Should generate at least 2 Req DTO files, found: ${reqFiles*.name}"

File respDtoDir = new File(basedir, "src/main/java/com/example/dto/resp")
assert respDtoDir.exists() : "resp DTO directory should exist"
def respFiles = respDtoDir.listFiles().findAll { it.name.endsWith(".java") }
// create-product 和 get-product-detail 各有一个 Resp
assert respFiles.size() >= 2 : "Should generate at least 2 Resp DTO files, found: ${respFiles*.name}"

println "[multiple-init-decs] All assertions passed."
return true

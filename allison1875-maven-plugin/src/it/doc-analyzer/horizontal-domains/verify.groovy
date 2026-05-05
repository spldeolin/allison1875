/*
 * horizontal-domains 集成测试验证脚本
 * 水平划分：配置 2 个 domain（user & order），使用 -Ddomain=user 选择 user 领域运行。
 * 验证多 domain 配置下 -Ddomain 参数解析逻辑正确，以及 user 领域的文档正常生成。
 *
 * 注意：doc-analyzer 在单模块水平划分场景下，会遍历同一 sourceRoot 下的所有 controller，
 * 不按 controllerPackage 过滤。因此 order domain 的 controller 也会被检测到，这是预期行为。
 */

// 检查 build.log 确认 -Ddomain=user 被正确传递
File buildLog = new File(basedir, "build.log")
String logContent = buildLog.text
assert logContent.contains("domain=user"): "Build log should show domain=user was selected"

// 两个 domain 都在配置中
assert logContent.contains("\"name\" : \"user\""): "Config should contain user domain"
assert logContent.contains("\"name\" : \"order\""): "Config should contain order domain"

File apiDocsDir = new File(basedir, "api-docs")
assert apiDocsDir.exists(): "api-docs directory should exist"

def mdFiles = []
apiDocsDir.eachFileRecurse(groovy.io.FileType.FILES) { file ->
    if (file.name.endsWith(".md")) { mdFiles << file }
}
assert mdFiles.size() > 0: "At least one .md file should be generated"

String allContent = mdFiles.collect { it.text }.join("\n")

// 验证 md 文件数量（两个 controller → 两个 md 文件）
assert mdFiles.size() == 2: "Should generate 2 .md files, but found ${mdFiles.size()}"
def fileNames = mdFiles.collect { it.name }.sort()
assert fileNames.contains("用户管理.md"): "Should contain '用户管理.md'"
assert fileNames.contains("订单管理.md"): "Should contain '订单管理.md'"

// user domain's endpoints should be present
assert allContent.contains("创建用户"): "Should contain user domain handler '创建用户'"
assert allContent.contains("POST /api/users"): "Should contain user domain URL 'POST /api/users'"
assert allContent.contains("username"): "Should contain user request field 'username'"
assert allContent.contains("用户名"): "Should contain user request field comment '用户名'"

// In single-module horizontal split, ALL controllers in sourceRoot are detected (expected behavior)
// OrderController is also detected since it's under the same sourceRoot
assert allContent.contains("创建订单"): "OrderController should also be detected in single-module scenario"
assert allContent.contains("POST /api/orders"): "Should contain order domain URL 'POST /api/orders'"
assert allContent.contains("productId"): "Should contain order request field 'productId'"
assert allContent.contains("商品ID"): "Should contain order request field comment '商品ID'"

println "[horizontal-domains] All assertions passed."
return true

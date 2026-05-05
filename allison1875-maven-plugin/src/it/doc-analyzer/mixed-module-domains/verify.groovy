/*
 * mixed-module-domains 集成测试验证脚本
 * 混合划分：多 domain + 垂直拆分
 * user domain: controller 在 user-web/，DTO 在 user-api/
 * order domain: controller 在 order-web/，DTO 在 order-api/
 * 使用 -Ddomain=order 运行 order 领域
 *
 * 验证：
 * 1. 多 domain + 垂直拆分配置下 -Ddomain 选择正确
 * 2. order domain 的 sourceRoot 正确解析（order-web + order-api）
 * 3. order domain 的 controller 和 DTO 能跨模块关联
 */

File buildLog = new File(basedir, "build.log")
String logContent = buildLog.text

// 确认选择了 order domain
assert logContent.contains("domain=order"): "Build log should show domain=order was selected"

// allSourceRoots 应包含 order-api（DTO 子模块）
assert logContent.contains("order-api"): "allSourceRoots should include order-api module"

File apiDocsDir = new File(basedir, "api-docs")
assert apiDocsDir.exists(): "api-docs directory should exist"

def mdFiles = []
apiDocsDir.eachFileRecurse(groovy.io.FileType.FILES) { file ->
    if (file.name.endsWith(".md")) { mdFiles << file }
}
assert mdFiles.size() > 0: "At least one .md file should be generated"

String allContent = mdFiles.collect { it.text }.join("\n")

// 验证 md 文件数量
assert mdFiles.size() == 2: "Should generate 2 .md files (both controllers detected in same sourceRoot), but found ${mdFiles.size()}"

// Order domain's endpoints should be present
assert allContent.contains("下单"): "Should contain order domain handler '下单'"
assert allContent.contains("POST /api/orders"): "Should contain 'POST /api/orders'"

// Order DTO fields from order-api module should be resolved
assert allContent.contains("productId"): "Should contain order request field 'productId'"
assert allContent.contains("shippingAddress"): "Should contain order request field 'shippingAddress'"
assert allContent.contains("quantity"): "Should contain order request field 'quantity'"

// Response fields from order-api
assert allContent.contains("orderNo"): "Should contain order response field 'orderNo'"

println "[mixed-module-domains] All assertions passed."
return true

/*
 * vertical-modules 集成测试验证脚本
 * 垂直划分：controller 在 controller-web/，DTO 在 dto-api/
 * 验证 controllerModule 和 dtoModule 配置能正确解析不同目录的 sourceRoot，
 * AstForest 能跨多个 sourceRoot 解析 controller 和 DTO 的 AST。
 */

// 检查 build.log 确认多 sourceRoot 被正确发现
File buildLog = new File(basedir, "build.log")
String logContent = buildLog.text

// allSourceRoots 应包含 dto-api 路径（DTO 子模块）
assert logContent.contains("dto-api"): "allSourceRoots should include dto-api module"

File apiDocsDir = new File(basedir, "api-docs")
assert apiDocsDir.exists(): "api-docs directory should exist"

def mdFiles = []
apiDocsDir.eachFileRecurse(groovy.io.FileType.FILES) { file ->
    if (file.name.endsWith(".md")) { mdFiles << file }
}
assert mdFiles.size() > 0: "At least one .md file should be generated"

String allContent = mdFiles.collect { it.text }.join("\n")

// Controller from controller-web module should be detected
assert allContent.contains("创建商品"): "Should contain handler from controller-web module"
assert allContent.contains("POST /api/products"): "Should contain 'POST /api/products'"

// DTO fields from dto-api module should be resolved in documentation
assert allContent.contains("productName"): "Should contain DTO field 'productName' from dto-api module"
assert allContent.contains("price"): "Should contain DTO field 'price' from dto-api module"
assert allContent.contains("description"): "Should contain DTO field 'description' from dto-api module"

// 验证 Response Body 字段
assert allContent.contains("id"): "Should contain response field 'id'"

// 验证 md 文件名
def fileNames = mdFiles.collect { it.name }.sort()
assert fileNames.contains("商品管理.md"): "Should contain '商品管理.md'"

println "[vertical-modules] All assertions passed."
return true

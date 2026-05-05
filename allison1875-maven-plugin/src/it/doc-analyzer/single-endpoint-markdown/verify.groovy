/*
 * single-endpoint-markdown 集成测试验证脚本
 *
 * 验证 doc-analyzer 能正确处理：
 * - singleEndpointPerMarkdown = true 时，每个 endpoint 生成独立的 md 文件
 * - globalUrlPrefix 不以 / 开头时的处理（自动补 /）
 * - @RequestParam 的 name / value 别名
 * - @RequestParam 的 defaultValue
 * - #API-DOC-IGNORE# 字段忽略
 * - void 返回类型（无 Response Body）
 */

File apiDocsDir = new File(basedir, "api-docs")
assert apiDocsDir.exists() : "api-docs directory should exist"

def mdFiles = []
apiDocsDir.eachFileRecurse(groovy.io.FileType.FILES) { file ->
    if (file.name.endsWith(".md")) {
        mdFiles << file
    }
}

// singleEndpointPerMarkdown = true 时，2 个 handler 应生成 2 个 md 文件
assert mdFiles.size() == 2 : "Should generate 2 .md files (one per endpoint), but found ${mdFiles.size()}"

String allContent = mdFiles.collect { it.text }.join("\n")

// 验证两个 handler 都被分析
assert allContent.contains("搜索物品") : "Should contain '搜索物品'"
assert allContent.contains("提交物品") : "Should contain '提交物品'"

// 验证 globalUrlPrefix 不以 / 开头时自动补 /（config 中 globalUrlPrefix: api → 自动补/）
assert allContent.contains("/api/items") : "Should contain prefixed URL '/api/items'"
assert allContent.contains("GET /api/items/search") : "Should contain 'GET /api/items/search'"
assert allContent.contains("POST /api/items") : "Should contain 'POST /api/items'"

// 验证 @RequestParam(name = "q") 别名
assert allContent.contains("|q|") : "Should contain aliased query param name 'q' in table row"

// 验证 @RequestParam(value = "page", defaultValue = "1")
assert allContent.contains("page") : "Should contain query param 'page'"
assert allContent.contains("1") : "Should contain defaultValue '1'"

// 验证 #API-DOC-IGNORE# 字段不出现
assert !allContent.contains("internalTraceId") : "Should NOT contain ignored field 'internalTraceId'"

// 验证 keyword 字段存在（请求体）
assert allContent.contains("keyword") : "Should contain field 'keyword'"

// 验证 void 返回类型的 handler（submitItem）不生成 Response Body 区域
// submitItem 的 md 文件不应包含 "Response Body"
def submitMd = mdFiles.find { it.text.contains("提交物品") }
assert submitMd != null : "Should find md file containing '提交物品'"
assert !submitMd.text.contains("Response Body") : "void handler should NOT have Response Body section"

println "[single-endpoint-markdown] All assertions passed."
return true

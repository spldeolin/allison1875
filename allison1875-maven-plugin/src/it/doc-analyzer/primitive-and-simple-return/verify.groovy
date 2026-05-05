/*
 * primitive-and-simple-return 集成测试验证脚本
 */
File apiDocsDir = new File(basedir, "api-docs")
assert apiDocsDir.exists() : "api-docs directory should exist"

def mdFiles = []
apiDocsDir.eachFileRecurse(groovy.io.FileType.FILES) { file ->
    if (file.name.endsWith(".md")) { mdFiles << file }
}
assert mdFiles.size() > 0 : "At least one .md file should be generated"

String allContent = mdFiles.collect { it.text }.join("\n")

// 验证 md 文件名和数量
assert mdFiles.size() == 1 : "Should generate exactly 1 .md file, but found ${mdFiles.size()}"
assert mdFiles[0].name == "健康检查.md" : "Markdown filename should be '健康检查.md'"

// 验证所有 handler
assert allContent.contains("返回纯字符串") : "Should contain '返回纯字符串'"
assert allContent.contains("返回计数") : "Should contain '返回计数'"
assert allContent.contains("返回是否健康") : "Should contain '返回是否健康'"

// 验证 URL
assert allContent.contains("GET /api/health/status") : "Should contain 'GET /api/health/status'"
assert allContent.contains("GET /api/health/count") : "Should contain 'GET /api/health/count'"
assert allContent.contains("GET /api/health/alive") : "Should contain 'GET /api/health/alive'"

// 验证 String 返回类型被正确解析（isValueTypeSchema 分支）
assert allContent.contains("| | String |") : "Should contain simple String value type schema row"

// 验证 Integer 返回类型
assert allContent.contains("| | Integer |") : "Should contain simple Integer value type schema row"

// 验证 Boolean 返回类型
assert allContent.contains("| | Boolean |") : "Should contain simple Boolean value type schema row"

// 验证 @return 描述被提取
assert allContent.contains("健康状态") : "Should contain @return description '健康状态'"
assert allContent.contains("在线用户数") : "Should contain @return description '在线用户数'"
assert allContent.contains("是否正常") : "Should contain @return description '是否正常'"

// 验证没有 Request Body 部分（三个都是 GET 且没有 @RequestBody）
assert !allContent.contains("Request Body") : "Should NOT contain 'Request Body' for GET handlers without request body"

println "[primitive-and-simple-return] All assertions passed."
return true

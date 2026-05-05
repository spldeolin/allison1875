/*
 * no-controller-mapping 集成测试验证脚本
 */
File apiDocsDir = new File(basedir, "api-docs")
assert apiDocsDir.exists() : "api-docs directory should exist"

def mdFiles = []
apiDocsDir.eachFileRecurse(groovy.io.FileType.FILES) { file ->
    if (file.name.endsWith(".md")) { mdFiles << file }
}
assert mdFiles.size() > 0 : "At least one .md file should be generated"

String allContent = mdFiles.collect { it.text }.join("\n")

// 验证 md 文件名
assert mdFiles.size() == 1 : "Should generate exactly 1 .md file, but found ${mdFiles.size()}"
assert mdFiles[0].name == "无类级RequestMapping.md" : "Markdown filename should be '无类级RequestMapping.md'"

assert allContent.contains("Ping接口") : "Should contain 'Ping接口'"
assert allContent.contains("Version接口") : "Should contain 'Version接口'"

// 验证 URL 正确（没有 controller 级前缀，直接使用 method 级路径）
assert allContent.contains("GET /ping") : "Should contain 'GET /ping'"
assert allContent.contains("GET /version") : "Should contain 'GET /version'"

// 验证 Response Body 字段
assert allContent.contains("message") : "Should contain response field 'message'"
assert allContent.contains("响应消息") : "Should contain response field comment '响应消息'"
assert allContent.contains("timestamp") : "Should contain response field 'timestamp'"
assert allContent.contains("时间戳") : "Should contain response field comment '时间戳'"

println "[no-controller-mapping] All assertions passed."
return true

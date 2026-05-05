/*
 * request-mapping-params 集成测试验证脚本
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
assert mdFiles[0].name == "配置管理.md" : "Markdown filename should be '配置管理.md'"

assert allContent.contains("查询配置") : "Should contain '查询配置'"

// 验证完整的 URL 包含 params（controller 级 + method 级拼接）
assert allContent.contains("GET /api/config?module=system&action=read") : "Should contain full URL 'GET /api/config?module=system&action=read'"

// 验证 Response Body 字段
assert allContent.contains("configKey") : "Should contain response field 'configKey'"
assert allContent.contains("配置键") : "Should contain response field comment '配置键'"
assert allContent.contains("configValue") : "Should contain response field 'configValue'"
assert allContent.contains("配置值") : "Should contain response field comment '配置值'"

println "[request-mapping-params] All assertions passed."
return true

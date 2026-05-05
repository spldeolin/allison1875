/*
 * dependency-dirs 集成测试验证脚本
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
assert mdFiles[0].name == "审计管理.md" : "Markdown filename should be '审计管理.md'"

assert allContent.contains("查询审计信息") : "Should contain '查询审计信息'"
assert allContent.contains("GET /api/audit") : "Should contain 'GET /api/audit'"

// 验证 Response Body 字段
assert allContent.contains("auditId") : "Should contain field 'auditId'"
assert allContent.contains("审计ID") : "Should contain field comment '审计ID'"
assert allContent.contains("actionType") : "Should contain field 'actionType'"
assert allContent.contains("操作类型") : "Should contain field comment '操作类型'"

// 验证没有 Request Body（GET 无 @RequestBody）
assert !allContent.contains("Request Body") : "Should NOT contain 'Request Body' for GET handler without request body"

println "[dependency-dirs] All assertions passed."
return true

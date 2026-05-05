File apiDocsDir = new File(basedir, "api-docs")
assert apiDocsDir.exists(): "api-docs directory should exist"

def mdFiles = []
apiDocsDir.eachFileRecurse(groovy.io.FileType.FILES) { file ->
    if (file.name.endsWith(".md")) { mdFiles << file }
}
assert mdFiles.size() > 0: "At least one .md file should be generated"

String allContent = mdFiles.collect { it.text }.join("\n")

// 验证 md 文件名和数量
assert mdFiles.size() == 1: "Should generate exactly 1 .md file, but found ${mdFiles.size()}"
assert mdFiles[0].name == "地址管理.md": "Markdown filename should be '地址管理.md'"

// Endpoint detected
assert allContent.contains("创建地址"): "Should contain handler description"
assert allContent.contains("POST /api/addresses"): "Should contain 'POST /api/addresses'"

// Record components from request body should appear as fields with Javadoc @param comments
assert allContent.contains("title"): "Should contain record component 'title'"
assert allContent.contains("地址标题"): "Should contain record component comment '地址标题'"
assert allContent.contains("city"): "Should contain record component 'city'"
assert allContent.contains("城市"): "Should contain record component comment '城市'"
assert allContent.contains("zipCode"): "Should contain record component 'zipCode'"
assert allContent.contains("邮编"): "Should contain record component comment '邮编'"

// Response record components
assert allContent.contains("id"): "Should contain response record component 'id'"
assert allContent.contains("ID"): "Should contain response field comment 'ID'"

// 验证 Markdown 结构
assert allContent.contains("### Request Body (application/json)"): "Should contain Request Body section"
assert allContent.contains("### Response Body (application/json)"): "Should contain Response Body section"

println "[java-record] All assertions passed."
return true

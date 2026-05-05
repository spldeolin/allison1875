/*
 * json-property-access 集成测试验证脚本
 */

File apiDocsDir = new File(basedir, "api-docs")
assert apiDocsDir.exists() : "api-docs directory should exist"

def mdFiles = []
apiDocsDir.eachFileRecurse(groovy.io.FileType.FILES) { file ->
    if (file.name.endsWith(".md")) { mdFiles << file }
}
assert mdFiles.size() > 0 : "At least one .md file should be generated"

String allContent = mdFiles.collect { it.text }.join("\n")

// 找到 Request Body 区域和 Response Body 区域
// 简单方式：检查整个文档
assert allContent.contains("创建事件") : "Should contain '创建事件'"

// 验证 md 文件名和数量
assert mdFiles.size() == 1 : "Should generate exactly 1 .md file, but found ${mdFiles.size()}"
assert mdFiles[0].name == "事件管理.md" : "Markdown filename should be '事件管理.md'"

// 验证 URL 和 HTTP 方法
assert allContent.contains("POST /api/events") : "Should contain 'POST /api/events'"

// @JsonProperty(access=READ_ONLY) should NOT appear in Request Body (before Response Body section)
String reqSection = allContent.substring(0, allContent.indexOf("Response Body"))
assert !reqSection.contains("readOnlyField") : "readOnlyField should NOT appear in Request Body section"

// @JsonProperty(access=READ_ONLY) should appear in Response Body
String respSection = allContent.substring(allContent.indexOf("Response Body"))
assert respSection.contains("readOnlyField") : "readOnlyField should appear in Response Body section"

// @JsonProperty(access=WRITE_ONLY) fields are not included in JsonSchema output
// Because doc-analyzer generates JsonSchema via ObjectMapper which filters by access mode.
// In the Req DTO, writeOnlyField (WRITE_ONLY) does not appear in the generated schema.
// In the Resp DTO, writeOnlyForResp (WRITE_ONLY) also does not appear.
assert !reqSection.contains("writeOnlyField") : "writeOnlyField should NOT appear in Request Body (filtered by JsonSchema generation)"
assert !respSection.contains("writeOnlyForResp") : "writeOnlyForResp should NOT appear in Response Body section"

// @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss") should appear as format documentation
assert allContent.contains("格式：yyyy-MM-dd HH:mm:ss") : "Should contain @JsonFormat pattern as '格式：yyyy-MM-dd HH:mm:ss'"

// eventName should appear in both request and response
assert reqSection.contains("eventName") : "Request Body should contain 'eventName'"
assert respSection.contains("eventName") : "Response Body should contain 'eventName'"

// 验证字段注释
assert allContent.contains("事件名称") : "Should contain field comment '事件名称'"
assert allContent.contains("事件开始时间") : "Should contain field comment '事件开始时间'"

println "[json-property-access] All assertions passed."
return true

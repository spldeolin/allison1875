/*
 * controller-with-response-body 集成测试验证脚本
 *
 * 验证 @Controller + @ResponseBody（非 @RestController）的场景
 */

File apiDocsDir = new File(basedir, "api-docs")
assert apiDocsDir.exists() : "api-docs directory should exist"

def mdFiles = []
apiDocsDir.eachFileRecurse(groovy.io.FileType.FILES) { file ->
    if (file.name.endsWith(".md")) {
        mdFiles << file
    }
}
assert mdFiles.size() > 0 : "At least one .md file should be generated"

String allContent = mdFiles.collect { it.text }.join("\n")

// 验证 md 文件名和数量
assert mdFiles.size() == 1 : "Should generate exactly 1 .md file, but found ${mdFiles.size()}"
assert mdFiles[0].name == "报表管理.md" : "Markdown filename should be '报表管理.md'"

// 验证两个有 @ResponseBody 的 handler 都被解析
assert allContent.contains("查询报表（有@ResponseBody）") : "Should contain '查询报表（有@ResponseBody）'"
assert allContent.contains("创建报表（有@ResponseBody）") : "Should contain '创建报表（有@ResponseBody）'"

// 验证 HTTP 方法
assert allContent.contains("GET /api/reports") : "Should contain 'GET /api/reports'"
assert allContent.contains("POST /api/reports") : "Should contain 'POST /api/reports'"

// 验证 Response Body 字段
assert allContent.contains("reportName") : "Should contain response field 'reportName'"
assert allContent.contains("报表名称") : "Should contain response field comment '报表名称'"
assert allContent.contains("id") : "Should contain response field 'id'"

// 验证 Request Body 字段
assert allContent.contains("reportType") : "Should contain request field 'reportType'"
assert allContent.contains("报表类型") : "Should contain request field comment '报表类型'"

// 验证 Markdown 结构（两个 handler 都有 Response Body）
int respBodyCount = (allContent =~ /Response Body/).count
assert respBodyCount == 2 : "Should have 2 'Response Body' sections (both handlers have @ResponseBody), but found ${respBodyCount}"

println "[controller-with-response-body] All assertions passed."
return true

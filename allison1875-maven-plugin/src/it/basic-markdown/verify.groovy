/*
 * basic-markdown 集成测试验证脚本
 *
 * 验证 doc-analyzer 以 MARKDOWN 模式输出后，api-docs/ 目录下生成了正确的 .md 文件。
 */

File apiDocsDir = new File(basedir, "api-docs")
assert apiDocsDir.exists() : "api-docs directory should exist"
assert apiDocsDir.isDirectory() : "api-docs should be a directory"

// 查找所有 md 文件
def mdFiles = []
apiDocsDir.eachFileRecurse(groovy.io.FileType.FILES) { file ->
    if (file.name.endsWith(".md")) {
        mdFiles << file
    }
}
assert mdFiles.size() > 0 : "At least one .md file should be generated"

// 将所有 md 内容合并，方便统一断言
String allContent = mdFiles.collect { it.text }.join("\n")

// 验证包含 GET handler 的文档
assert allContent.contains("GET") : "Markdown should contain HTTP method GET"
assert allContent.contains("/api/users") : "Markdown should contain URL /api/users"
assert allContent.contains("查询用户列表") : "Markdown should contain handler description '查询用户列表'"

// 验证包含 POST handler 的文档
assert allContent.contains("POST") : "Markdown should contain HTTP method POST"
assert allContent.contains("创建用户") : "Markdown should contain handler description '创建用户'"

// 验证 Request Body 字段出现在文档中
assert allContent.contains("username") : "Markdown should contain field 'username'"
assert allContent.contains("age") : "Markdown should contain field 'age'"
assert allContent.contains("email") : "Markdown should contain field 'email'"

// 验证 Response Body 字段
assert allContent.contains("id") : "Markdown should contain response field 'id'"

// 验证 Query Param 出现（keyword 参数）
assert allContent.contains("keyword") : "Markdown should contain query param 'keyword'"

println "[basic-markdown] All assertions passed."
return true

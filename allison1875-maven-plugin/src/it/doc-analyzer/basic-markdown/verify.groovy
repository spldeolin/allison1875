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

// 验证精确生成了1个 md 文件，文件名来自 controller javadoc 首行
assert mdFiles.size() == 1 : "Should generate exactly 1 .md file, but found ${mdFiles.size()}"
assert mdFiles[0].name == "用户管理.md" : "Markdown filename should be '用户管理.md'"

// 验证包含 GET handler 的文档
assert allContent.contains("GET") : "Markdown should contain HTTP method GET"
assert allContent.contains("/api/users") : "Markdown should contain URL /api/users"
assert allContent.contains("查询用户列表") : "Markdown should contain handler description '查询用户列表'"
assert allContent.contains("根据关键字搜索用户") : "Markdown should contain handler sub-description"

// 验证包含 POST handler 的文档
assert allContent.contains("POST") : "Markdown should contain HTTP method POST"
assert allContent.contains("创建用户") : "Markdown should contain handler description '创建用户'"

// 验证 Request Body 字段出现在文档中
assert allContent.contains("username") : "Markdown should contain field 'username'"
assert allContent.contains("age") : "Markdown should contain field 'age'"
assert allContent.contains("email") : "Markdown should contain field 'email'"
assert allContent.contains("用户名") : "Markdown should contain field comment '用户名'"

// 验证校验注解的文档化
assert allContent.contains("必须有非空格字符") : "Markdown should contain @NotBlank validation description"
assert allContent.contains("不能为null") : "Markdown should contain @NotNull validation description"

// 验证 Response Body 字段（List<UserResp> → Object Array）
assert allContent.contains("id") : "Markdown should contain response field 'id'"
assert allContent.contains("Object Array") : "Markdown should contain 'Object Array' for List return type"
assert allContent.contains("用户ID") : "Markdown should contain response field comment '用户ID'"

// 验证 Query Param 出现（keyword 参数）
assert allContent.contains("keyword") : "Markdown should contain query param 'keyword'"
assert allContent.contains("否") : "Markdown should contain 'required=false' rendered as '否'"

// 验证 Markdown 结构标记
assert allContent.contains("### URL") : "Markdown should contain '### URL' section header"
assert allContent.contains("### Query Param") : "Markdown should contain '### Query Param' section header"
assert allContent.contains("### Request Body (application/json)") : "Markdown should contain request body section"
assert allContent.contains("### Response Body (application/json)") : "Markdown should contain response body section"
assert allContent.contains("---") : "Markdown should contain separator '---'"

println "[basic-markdown] All assertions passed."
return true

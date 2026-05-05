/*
 * pathvar-and-reqparam-aliases 集成测试验证脚本
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
assert mdFiles[0].name == "图书管理.md" : "Markdown filename should be '图书管理.md'"

// 验证 3 个 handler 都被解析
assert allContent.contains("通过SingleMember别名查询图书") : "Should contain description"
assert allContent.contains("通过name属性别名查询图书") : "Should contain description"
assert allContent.contains("搜索图书") : "Should contain description"

// 验证 URL 中包含路径变量
assert allContent.contains("GET /api/books/{bookId}") : "Should contain URL with path var 'GET /api/books/{bookId}'"
assert allContent.contains("GET /api/books/by-isbn/{isbn}") : "Should contain URL with path var 'GET /api/books/by-isbn/{isbn}'"
assert allContent.contains("GET /api/books/search") : "Should contain URL 'GET /api/books/search'"

// 验证 Path Param 表格
assert allContent.contains("### Path Param") : "Should contain Path Param section"
assert allContent.contains("bookId") : "Should contain aliased path param name 'bookId'"
assert allContent.contains("isbn") : "Should contain aliased path param name 'isbn'"

// 验证 Query Param 表格
assert allContent.contains("### Query Param") : "Should contain Query Param section"
assert allContent.contains("keyword") : "Should contain aliased query param name 'keyword'"
assert allContent.contains("是") : "Should contain required=true rendered as '是'"

// 验证 @RequestParam(value = "page_no") 别名
assert allContent.contains("page_no") : "Should contain aliased query param name 'page_no'"
assert allContent.contains("页码") : "Should contain query param description '页码'"

// 验证 primitive boolean 类型推导
assert allContent.contains("active") : "Should contain query param 'active'"
assert allContent.contains("Boolean") : "Should contain type 'Boolean' for primitive boolean"
assert allContent.contains("是否启用") : "Should contain query param description '是否启用'"

println "[pathvar-and-reqparam-aliases] All assertions passed."
return true

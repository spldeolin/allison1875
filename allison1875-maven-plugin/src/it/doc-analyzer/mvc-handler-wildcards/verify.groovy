/*
 * mvc-handler-wildcards 集成测试验证脚本
 *
 * 验证 mvcHandlerQualifierWildcards 过滤：
 * - 只有匹配 *.list* 或 *.get{ById,Detail} 的 handler 出现在文档中
 * - createAnimal 和 deleteAnimal 不应出现在文档中
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
assert mdFiles[0].name == "动物管理.md" : "Markdown filename should be '动物管理.md'"

// 验证匹配的 handler 出现在文档中
assert allContent.contains("查询动物列表") : "Should contain '查询动物列表' (listAnimals matches *.list*)"
assert allContent.contains("根据ID查询动物详情") : "Should contain '根据ID查询动物详情' (getById matches *.get{ById,Detail})"

// 验证匹配 handler 的 URL
assert allContent.contains("GET /api/animals") : "Should contain 'GET /api/animals'"
assert allContent.contains("GET /api/animals/{id}") : "Should contain 'GET /api/animals/{id}'"

// 验证匹配 handler 的 Path Param
assert allContent.contains("### Path Param") : "Should contain Path Param section for getById"

// 验证匹配 handler 的 Response Body 字段
assert allContent.contains("name") : "Should contain response field 'name'"
assert allContent.contains("动物名称") : "Should contain response field comment '动物名称'"
assert allContent.contains("species") : "Should contain response field 'species'"
assert allContent.contains("动物种类") : "Should contain response field comment '动物种类'"

// 验证 Object Array (listAnimals 返回 List<AnimalResp>)
assert allContent.contains("Object Array") : "Should contain 'Object Array' for List return type"

// 验证不匹配的 handler 不出现在文档中
assert !allContent.contains("创建动物") : "Should NOT contain '创建动物' (createAnimal does not match wildcards)"
assert !allContent.contains("删除动物") : "Should NOT contain '删除动物' (deleteAnimal does not match wildcards)"
assert !allContent.contains("POST") : "Should NOT contain HTTP method POST (createAnimal filtered)"
assert !allContent.contains("DELETE") : "Should NOT contain HTTP method DELETE (deleteAnimal filtered)"

println "[mvc-handler-wildcards] All assertions passed."
return true

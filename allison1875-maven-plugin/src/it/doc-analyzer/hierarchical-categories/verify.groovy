/*
 * hierarchical-categories 集成测试验证脚本
 */

File apiDocsDir = new File(basedir, "api-docs")
assert apiDocsDir.exists() : "api-docs directory should exist"

// 验证层级目录结构：api-docs/后台管理模块/系统设置.md
File hierarchicalDir = new File(apiDocsDir, "后台管理模块")
assert hierarchicalDir.exists() : "Hierarchical category directory '后台管理模块' should exist"
assert hierarchicalDir.isDirectory() : "'后台管理模块' should be a directory"

// 验证精确的 md 文件
File settingMd = new File(hierarchicalDir, "系统设置.md")
assert settingMd.exists() : "系统设置.md should exist under '后台管理模块'"
assert settingMd.isFile() : "系统设置.md should be a file"

def mdFiles = []
hierarchicalDir.eachFileRecurse(groovy.io.FileType.FILES) { file ->
    if (file.name.endsWith(".md")) { mdFiles << file }
}
assert mdFiles.size() == 1 : "Should generate exactly 1 .md file under hierarchical dir, but found ${mdFiles.size()}"

String allContent = mdFiles.collect { it.text }.join("\n")
assert allContent.contains("查询系统设置") : "Should contain '查询系统设置'"
assert allContent.contains("GET /api/admin/settings") : "Should contain 'GET /api/admin/settings'"

// 验证 Response Body 字段
assert allContent.contains("key") : "Should contain response field 'key'"
assert allContent.contains("设置键") : "Should contain response field comment '设置键'"
assert allContent.contains("value") : "Should contain response field 'value'"
assert allContent.contains("设置值") : "Should contain response field comment '设置值'"

// 验证 api-docs 根目录下没有直接放置 md 文件（全部在子目录中）
def rootMdFiles = []
apiDocsDir.eachFile(groovy.io.FileType.FILES) { file ->
    if (file.name.endsWith(".md")) { rootMdFiles << file }
}
assert rootMdFiles.size() == 0 : "Root api-docs dir should NOT contain .md files directly, all should be in hierarchical subdirs"

println "[hierarchical-categories] All assertions passed."
return true

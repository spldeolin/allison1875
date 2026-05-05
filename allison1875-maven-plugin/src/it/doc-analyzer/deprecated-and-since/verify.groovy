/*
 * deprecated-and-since 集成测试验证脚本
 *
 * 验证 doc-analyzer 能正确处理：
 * - handler 级和 controller 级的 @since 标签
 * - handler 级的 @deprecated 标签
 * - 字段级的 @since 和 @deprecated 标签
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

String mdContent = mdFiles.collect { it.text }.join("\n")

// 验证 md 文件名和数量
assert mdFiles.size() == 1 : "Should generate exactly 1 .md file, but found ${mdFiles.size()}"
assert mdFiles[0].name == "公告管理.md" : "Markdown filename should be '公告管理.md'"

// 验证3个 handler 都被分析
assert mdContent.contains("查询最新公告") : "Should contain '查询最新公告'"
assert mdContent.contains("创建公告") : "Should contain '创建公告'"
assert mdContent.contains("查询过期公告（已废弃）") : "Should contain '查询过期公告（已废弃）'"

// 验证 URL
assert mdContent.contains("GET /api/notices/latest") : "Should contain 'GET /api/notices/latest'"
assert mdContent.contains("POST /api/notices") : "Should contain 'POST /api/notices'"
assert mdContent.contains("GET /api/notices/expired") : "Should contain 'GET /api/notices/expired'"

// 验证 handler 级 @since 标签 — 兼容性说明区域
assert mdContent.contains("### 兼容性说明") : "Should contain compatibility section header"
assert mdContent.contains("本接口加入版本：v1.0.0") : "Should contain '本接口加入版本：v1.0.0'"
assert mdContent.contains("本接口加入版本：v2.0.0") : "Should contain '本接口加入版本：v2.0.0'"

// 验证 handler 级 @deprecated 标签 — 兼容性说明区域
assert mdContent.contains("本接口已过时，不建议调用，过时原因：") : "Should contain deprecated description prefix"
assert mdContent.contains("getLatest") : "Deprecated description should reference 'getLatest'"
assert mdContent.contains("v4.0") : "Deprecated description should reference 'v4.0'"

// 验证字段级 @since（priority 字段的 @since v3.0.0）
assert mdContent.contains("本字段加入版本：v3.0.0") : "Should contain field-level @since 'v3.0.0'"

// 验证字段级 @deprecated（category 字段的 @deprecated）
assert mdContent.contains("本字段已过时，原因：") : "Should contain field-level @deprecated prefix"
assert mdContent.contains("tags") : "Should contain field-level @deprecated referencing 'tags'"

// 验证 Request Body 字段
assert mdContent.contains("title") : "Should contain field 'title'"
assert mdContent.contains("公告标题") : "Should contain field comment '公告标题'"
assert mdContent.contains("content") : "Should contain field 'content'"
assert mdContent.contains("公告内容") : "Should contain field comment '公告内容'"
assert mdContent.contains("priority") : "Should contain field 'priority'"
assert mdContent.contains("category") : "Should contain deprecated field 'category'"

println "[deprecated-and-since] All assertions passed."
return true

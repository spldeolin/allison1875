/*
 * enum-and-validation 集成测试验证脚本
 *
 * 验证 doc-analyzer 能正确处理：
 * - 枚举字段的枚举项分析（EnumServiceImpl）
 * - @Size, @Min, @Max, @Pattern 等校验注解
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
assert mdFiles[0].name == "任务管理.md" : "Markdown filename should be '任务管理.md'"

// 验证 handler
assert mdContent.contains("创建任务") : "Should contain '创建任务'"
assert mdContent.contains("POST /api/tasks") : "Should contain 'POST /api/tasks'"

// 验证枚举项出现在文档中（TaskStatusEnum 的 code : title 格式）
assert mdContent.contains("1 : 待处理") : "Should contain enum constant '1 : 待处理'"
assert mdContent.contains("2 : 处理中") : "Should contain enum constant '2 : 处理中'"
assert mdContent.contains("3 : 已完成") : "Should contain enum constant '3 : 已完成'"
assert mdContent.contains("4 : 已取消") : "Should contain enum constant '4 : 已取消'"

// 验证 Response Body 中也出现枚举项
String respSection = mdContent.substring(mdContent.indexOf("Response Body"))
assert respSection.contains("1 : 待处理") : "Response Body should also contain enum constants"

// 验证 @NotBlank + @Size(min=1, max=200) 校验
assert mdContent.contains("必须有非空格字符") : "Should contain @NotBlank description"
assert mdContent.contains("最小长度/容量：1") : "Should contain @Size min=1"
assert mdContent.contains("最大长度/容量：200") : "Should contain @Size max=200"

// 验证 @Size(max=2000)
assert mdContent.contains("最大长度/容量：2000") : "Should contain @Size max=2000"

// 验证 @NotNull + @Min + @Max 校验
assert mdContent.contains("不能为null") : "Should contain @NotNull"
assert mdContent.contains("最小值：1") : "Should contain @Min(1)"
assert mdContent.contains("最大值：10") : "Should contain @Max(10)"

// 验证 @Pattern 校验（正则表达式）
assert mdContent.contains("正则表达式") : "Should contain @Pattern description prefix"
assert mdContent.contains("TASK-") : "Should contain @Pattern regexp reference 'TASK-'"

// 验证基本字段及其注释
assert mdContent.contains("title") : "Should contain field 'title'"
assert mdContent.contains("任务标题") : "Should contain field comment '任务标题'"
assert mdContent.contains("description") : "Should contain field 'description'"
assert mdContent.contains("任务描述") : "Should contain field comment '任务描述'"
assert mdContent.contains("status") : "Should contain field 'status'"
assert mdContent.contains("任务状态") : "Should contain field comment '任务状态'"
assert mdContent.contains("priority") : "Should contain field 'priority'"
assert mdContent.contains("budget") : "Should contain field 'budget'"
assert mdContent.contains("预算金额") : "Should contain field comment '预算金额'"
assert mdContent.contains("taskCode") : "Should contain field 'taskCode'"
assert mdContent.contains("任务编号") : "Should contain field comment '任务编号'"

println "[enum-and-validation] All assertions passed."
return true

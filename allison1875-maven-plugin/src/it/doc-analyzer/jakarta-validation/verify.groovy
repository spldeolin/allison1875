/*
 * jakarta-validation 集成测试验证脚本
 */
File apiDocsDir = new File(basedir, "api-docs")
assert apiDocsDir.exists() : "api-docs directory should exist"

def mdFiles = []
apiDocsDir.eachFileRecurse(groovy.io.FileType.FILES) { file ->
    if (file.name.endsWith(".md")) { mdFiles << file }
}
assert mdFiles.size() > 0 : "At least one .md file should be generated"

String allContent = mdFiles.collect { it.text }.join("\n")

// 验证 md 文件名和数量
assert mdFiles.size() == 1 : "Should generate exactly 1 .md file, but found ${mdFiles.size()}"
assert mdFiles[0].name == "会员管理.md" : "Markdown filename should be '会员管理.md'"

assert allContent.contains("创建会员") : "Should contain '创建会员'"
assert allContent.contains("POST /api/members") : "Should contain 'POST /api/members'"

// jakarta.validation.constraints.NotBlank + @Size(min=2, max=50)
assert allContent.contains("memberName") : "Should contain 'memberName'"
assert allContent.contains("会员名称") : "Should contain field comment '会员名称'"
assert allContent.contains("必须有非空格字符") : "Should contain @NotBlank description"
assert allContent.contains("最小长度/容量：2") : "Should contain @Size min=2"
assert allContent.contains("最大长度/容量：50") : "Should contain @Size max=50"

// jakarta.validation.constraints.NotNull + @Min(1) + @Max(5)
assert allContent.contains("不能为null") : "Should contain @NotNull description"
assert allContent.contains("最小值：1") : "Should contain @Min(1)"
assert allContent.contains("最大值：5") : "Should contain @Max(5)"

// jakarta.validation.constraints.Past
assert allContent.contains("必须是过去") : "Should contain @Past validator description"
assert allContent.contains("birthday") : "Should contain field 'birthday'"
assert allContent.contains("出生日期") : "Should contain field comment '出生日期'"

// 验证 Response Body
assert allContent.contains("id") : "Should contain response field 'id'"
assert allContent.contains("level") : "Should contain response field 'level'"
assert allContent.contains("会员等级") : "Should contain field comment '会员等级'"

println "[jakarta-validation] All assertions passed."
return true

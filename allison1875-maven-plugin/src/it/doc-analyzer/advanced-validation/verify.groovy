/*
 * advanced-validation 集成测试验证脚本
 */

File apiDocsDir = new File(basedir, "api-docs")
assert apiDocsDir.exists() : "api-docs directory should exist"

def mdFiles = []
apiDocsDir.eachFileRecurse(groovy.io.FileType.FILES) { file ->
    if (file.name.endsWith(".md")) { mdFiles << file }
}
assert mdFiles.size() > 0 : "At least one .md file should be generated"

String allContent = mdFiles.collect { it.text }.join("\n")

assert allContent.contains("创建支付") : "Should contain '创建支付'"

// 验证 md 文件名和数量
assert mdFiles.size() == 1 : "Should generate exactly 1 .md file, but found ${mdFiles.size()}"
assert mdFiles[0].name == "支付管理.md" : "Markdown filename should be '支付管理.md'"

// 验证 URL 和 HTTP 方法
assert allContent.contains("POST /api/payments") : "Should contain 'POST /api/payments'"

// @NotBlank + @Length(min=1, max=100)
assert allContent.contains("必须有非空格字符") : "Should contain @NotBlank description"
assert allContent.contains("最小长度/容量：1") : "Should contain @Length min=1"
assert allContent.contains("最大长度/容量：100") : "Should contain @Length max=100"

// @NotNull + @DecimalMin("0.01") + @DecimalMax("999999.99") + @Digits(integer=6, fraction=2)
assert allContent.contains("不能为null") : "Should contain @NotNull description"
assert allContent.contains("最小值：0.01") : "Should contain @DecimalMin value"
assert allContent.contains("最大值：999999.99") : "Should contain @DecimalMax value"
assert allContent.contains("最大整数位数：6") : "Should contain @Digits integer=6"
assert allContent.contains("最大小数位数：2") : "Should contain @Digits fraction=2"

// @Future
assert allContent.contains("必须是未来") : "Should contain @Future validator description"

// @Positive
assert allContent.contains("必须是正数") : "Should contain @Positive validator description"

// 集合元素上的校验 List<@NotBlank @Length(max=20) String>
assert allContent.contains("tags") : "Should contain field 'tags'"
assert allContent.contains("String Array") : "Should contain type 'String Array' for List<String>"
assert allContent.contains("列表内元素必须有非空格字符") : "Should contain collection element @NotBlank"
assert allContent.contains("列表内元素最大长度/容量：20") : "Should contain collection element @Length max=20"

// 验证 Request Body 字段注释
assert allContent.contains("支付描述") : "Should contain field comment '支付描述'"
assert allContent.contains("支付金额") : "Should contain field comment '支付金额'"
assert allContent.contains("预计支付时间") : "Should contain field comment '预计支付时间'"
assert allContent.contains("支付笔数") : "Should contain field comment '支付笔数'"
assert allContent.contains("标签列表") : "Should contain field comment '标签列表'"

// 验证 Response Body 字段
assert allContent.contains("id") : "Should contain response field 'id'"
assert allContent.contains("amount") : "Should contain response field 'amount'"
assert allContent.contains("status") : "Should contain response field 'status'"
assert allContent.contains("状态") : "Should contain response field comment '状态'"

println "[advanced-validation] All assertions passed."
return true

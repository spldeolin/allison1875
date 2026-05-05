/*
 * markdown-and-dsl 集成测试验证脚本
 *
 * 验证 doc-analyzer 同时以 MARKDOWN 和 DSL 模式输出：
 * - api-docs/ 目录生成 .md 文件
 * - api-dsls/ 目录生成 .json 文件
 * - CRUD 5个handler均被分析
 * - 嵌套 DTO 字段出现在文档中
 * - PathVariable 出现在文档中
 */
import groovy.json.JsonSlurper

// ==================== 验证 MARKDOWN 输出 ====================

File apiDocsDir = new File(basedir, "api-docs")
assert apiDocsDir.exists() : "api-docs directory should exist"
assert apiDocsDir.isDirectory() : "api-docs should be a directory"

def mdFiles = []
apiDocsDir.eachFileRecurse(groovy.io.FileType.FILES) { file ->
    if (file.name.endsWith(".md")) {
        mdFiles << file
    }
}
assert mdFiles.size() > 0 : "At least one .md file should be generated"

String mdContent = mdFiles.collect { it.text }.join("\n")

// 验证5个 CRUD handler 的文档标题
assert mdContent.contains("查询订单列表") : "Markdown should contain '查询订单列表'"
assert mdContent.contains("根据ID查询订单详情") : "Markdown should contain '根据ID查询订单详情'"
assert mdContent.contains("创建订单") : "Markdown should contain '创建订单'"
assert mdContent.contains("更新订单") : "Markdown should contain '更新订单'"
assert mdContent.contains("删除订单") : "Markdown should contain '删除订单'"

// 验证 URL
assert mdContent.contains("/api/orders") : "Markdown should contain URL /api/orders"

// 验证 PathVariable 出现在 Path Param 区域
assert mdContent.contains("orderId") : "Markdown should contain PathVariable 'orderId'"

// 验证嵌套 DTO 的字段（OrderItemReq 中的 productId、quantity）
assert mdContent.contains("productId") : "Markdown should contain nested field 'productId'"
assert mdContent.contains("quantity") : "Markdown should contain nested field 'quantity'"

// 验证请求体字段
assert mdContent.contains("shippingAddress") : "Markdown should contain field 'shippingAddress'"
assert mdContent.contains("buyerNote") : "Markdown should contain field 'buyerNote'"

// 验证响应体嵌套字段
assert mdContent.contains("productName") : "Markdown should contain nested response field 'productName'"
assert mdContent.contains("unitPrice") : "Markdown should contain nested response field 'unitPrice'"

// ==================== 验证 DSL 输出 ====================

File apiDslsDir = new File(basedir, "api-dsls")
assert apiDslsDir.exists() : "api-dsls directory should exist"
assert apiDslsDir.isDirectory() : "api-dsls should be a directory"

def jsonFiles = []
apiDslsDir.eachFileRecurse(groovy.io.FileType.FILES) { file ->
    if (file.name.endsWith(".json")) {
        jsonFiles << file
    }
}
assert jsonFiles.size() > 0 : "At least one .json file should be generated"

// 验证 JSON 合法且包含 endpoint 信息
def slurper = new JsonSlurper()
int totalEndpoints = 0
for (File jsonFile : jsonFiles) {
    def endpoints = slurper.parseText(jsonFile.text)
    assert endpoints != null : "JSON should be parseable: ${jsonFile.name}"
    assert endpoints instanceof List : "JSON root should be an array: ${jsonFile.name}"
    totalEndpoints += endpoints.size()
}

// CRUD 场景应该有5个endpoint
assert totalEndpoints == 5 : "Should have 5 endpoints in total (CRUD), but found ${totalEndpoints}"

println "[markdown-and-dsl] All assertions passed."
return true

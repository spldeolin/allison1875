File apiDocsDir = new File(basedir, "api-docs")
assert apiDocsDir.exists(): "api-docs directory should exist"

def mdFiles = []
apiDocsDir.eachFileRecurse(groovy.io.FileType.FILES) { file ->
    if (file.name.endsWith(".md")) { mdFiles << file }
}
assert mdFiles.size() > 0: "At least one .md file should be generated"

String allContent = mdFiles.collect { it.text }.join("\n")

// 验证 md 文件数量（3个 controller → 3个 md 文件）
assert mdFiles.size() == 3: "Should generate 3 .md files (one per controller), but found ${mdFiles.size()}"
def fileNames = mdFiles.collect { it.name }.sort()
assert fileNames.contains("商品管理.md"): "Should contain '商品管理.md'"
assert fileNames.contains("用户管理.md"): "Should contain '用户管理.md'"
assert fileNames.contains("订单管理.md"): "Should contain '订单管理.md'"

// Pattern "*Controller.list*" matches all three list* handlers
assert allContent.contains("列出订单"): "Should contain listOrders matched by *Controller.list*"
assert allContent.contains("列出用户"): "Should contain listUsers matched by *Controller.list*"
assert allContent.contains("列出商品"): "Should contain listProducts matched by *Controller.list*"

// Pattern "com.example.controller.{Order,User}Controller.get?rder*" matches getOrderDetail
assert allContent.contains("获取订单详情"): "Should contain getOrderDetail matched by {Order,User} and get?rder*"

// getUserDetail should NOT be matched (get?rder* won't match getUserDetail)
assert !allContent.contains("获取用户详情"): "Should NOT contain getUserDetail (not matching get?rder* glob)"

// 验证 URL
assert allContent.contains("GET /api/orders/list"): "Should contain URL 'GET /api/orders/list'"
assert allContent.contains("GET /api/users/list"): "Should contain URL 'GET /api/users/list'"
assert allContent.contains("GET /api/products/list"): "Should contain URL 'GET /api/products/list'"
assert allContent.contains("GET /api/orders/detail"): "Should contain URL 'GET /api/orders/detail'"

println "[glob-regex-branches] All assertions passed."
return true

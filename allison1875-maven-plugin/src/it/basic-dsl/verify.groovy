/*
 * basic-dsl 集成测试验证脚本
 *
 * 验证 doc-analyzer 以 DSL 模式输出后，api-dsls/ 目录下生成了正确的 .json 文件。
 */
import groovy.json.JsonSlurper

File apiDslsDir = new File(basedir, "api-dsls")
assert apiDslsDir.exists() : "api-dsls directory should exist"
assert apiDslsDir.isDirectory() : "api-dsls should be a directory"

// 查找所有 json 文件
def jsonFiles = []
apiDslsDir.eachFileRecurse(groovy.io.FileType.FILES) { file ->
    if (file.name.endsWith(".json")) {
        jsonFiles << file
    }
}
assert jsonFiles.size() > 0 : "At least one .json file should be generated"

// 验证每个 json 文件是合法 JSON 且包含 endpoint 信息
def slurper = new JsonSlurper()
boolean foundProductUrl = false
boolean foundGetMethod = false
boolean foundPostMethod = false

for (File jsonFile : jsonFiles) {
    def endpoints = slurper.parseText(jsonFile.text)
    assert endpoints != null : "JSON should be parseable: ${jsonFile.name}"
    assert endpoints instanceof List : "JSON root should be an array: ${jsonFile.name}"

    for (def endpoint : endpoints) {
        if (endpoint.urls != null) {
            for (def url : endpoint.urls) {
                if (url.contains("/api/products")) {
                    foundProductUrl = true
                }
            }
        }
        if (endpoint.httpMethod == "get") {
            foundGetMethod = true
        }
        if (endpoint.httpMethod == "post") {
            foundPostMethod = true
        }
    }
}

assert foundProductUrl : "DSL output should contain URL /api/products"
assert foundGetMethod : "DSL output should contain GET method"
assert foundPostMethod : "DSL output should contain POST method"

// 确保不会生成 markdown 目录（仅 DSL 模式）
File apiDocsDir = new File(basedir, "api-docs")
assert !apiDocsDir.exists() : "api-docs directory should NOT exist in DSL-only mode"

println "[basic-dsl] All assertions passed."
return true

# doc-analyzer / dependency-dirs

## 本用例在验证什么

在配置了 **`dependencyDirsOrJavaFilePath`**（指向 **Maven 默认编译源码根之外** 的额外 Java 目录）时，doc-analyzer 仍能完成扫描与 Markdown 生成；本例用并列的 **`external-dto/`** 模拟「依赖源码树 / 外挂 DTO 目录」一类场景。

## 功能点与分支关注点（逐条）

- **扩展分析范围**：`.allison1875.yml` 中声明额外目录后，插件不应因「非标准源码布局」而失败；主工程控制器与 `src/main/java` 下的 DTO 仍应被正常文档化。
- **单接口 GET、无请求体**：`GET /api/audit` 与方法说明「查询审计信息」写入 **`审计管理.md`**；**无 `@RequestBody`** 时，Markdown 中 **不出现**「Request Body」整块（避免给纯 GET 误加空请求体章节）。
- **响应体仍解析主工程 DTO**：`AuditResp` 的字段名（`auditId`、`actionType`）及 Javadoc（「审计ID」「操作类型」）出现在 Response Body 相关叙述中。
- **与外部目录的关系**：仓库内附带 `external-dto/com/external/dto/BaseInfo.java` 作为 **附加 Java 根** 的占位内容；本用例的 **断言重点** 在「带扩展目录配置时的主流程与 GET 无 body 分支」，不要求文档中必须出现外部类的字段（当前示例响应 DTO 也未引用该类）。

## 小结

本用例验证 **`dependencyDirsOrJavaFilePath` 配置可加载、不破坏生成**，并顺带锁定 **GET 且无请求体时不渲染 Request Body** 的文档行为。

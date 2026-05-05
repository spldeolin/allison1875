# doc-analyzer / json-property-access

## 本用例在验证什么

检查 doc-analyzer 在根据 **Jackson 语义** 生成接口文档时，能否尊重 **`@JsonProperty(access = …)`** 对「读 / 写 JSON」的划分，并把 **`@JsonFormat`** 的日期时间模式转成可读说明；避免出现「文档里仍列出实际不会出现在该方向 JSON 里的字段」。

## 功能点与分支关注点（逐条）

- **`READ_ONLY` 分侧展示**：请求 DTO 中带 **`READ_ONLY`** 的字段（本例 **`readOnlyField`**）在 **Request Body** 段落之前的内容里 **不得出现**；同一字段名在 **Response Body** 段落中 **应当出现**（服务端只出、客户端不送）。
- **`WRITE_ONLY` 与 Schema 路径**：请求 DTO 上的 **`WRITE_ONLY`** 字段（**`writeOnlyField`**）在 Request Body 侧 **不出现**；响应 DTO 上的 **`WRITE_ONLY`**（**`writeOnlyForResp`**）在 Response Body 侧 **不出现**——与「通过类似 `ObjectMapper` 的 JSON Schema / 可见性规则生成文档」的行为一致，避免把仅用于反序列化或内部写通道的字段画进错误的一栏。
- **`@JsonFormat`**：**`Date`** 上的 **`pattern`** 需在文档中体现为 **「格式：…」** 类固定话术（本例 **`yyyy-MM-dd HH:mm:ss`**），请求与响应里对 **`startTime`** 的说明均应覆盖到（整篇断言在全文上校验格式串）。
- **普通字段双向保留**：**`eventName`** 在 **Request Body** 与 **Response Body** 两段中都要出现；字段 Javadoc（如「事件名称」「事件开始时间」）仍正常展示。
- **产物与路由**：单文件 **`事件管理.md`**；**`POST /api/events`**；方法说明「创建事件」。

## 小结

本用例把 **Jackson 的读写可见性** 与 **日期格式注解** 接到同一条「创建事件」接口上，验证文档字段集合与 **真实 JSON 方向** 对齐，而不是简单罗列 Java 字段。

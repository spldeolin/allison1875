# doc-analyzer / java-record

## 本用例在验证什么

在 **Java 17 + `record`** 语法下，请求体与响应体使用 **紧凑数据载体（Record）** 而非普通 class/Lombok DTO 时，doc-analyzer 能否把 **record 分量（component）** 当作 API 字段展开，并从 **Record 上的 Javadoc `@param`** 取出中文说明写入 Markdown。

## 功能点与分支关注点（逐条）

- **运行与栈版本**：子工程以 **Java 17** 编译；Spring **6.x**、**Jakarta Validation** 命名空间，与配置里 **`javaVersion: "17"`**、**`enableJavaxMoveToJakarta: true`** 一致（该 IT 通常走 **`invoker-it-java17`** 一类 profile，需在 **JDK 17+** 环境执行）。
- **请求体为 Record**：`CreateAddressReq` 的 **`title` / `city` / `zipCode`** 出现在 Request Body 区块；说明文字来自 **`@param` 行**（如「地址标题」「城市」「邮编」）。
- **响应体为 Record**：`AddressResp` 的 **`id`** 等分量出现在 Response Body 区块；**`@param id` →「ID」** 等注释同样被采信。
- **接口元数据**：单文件 **`地址管理.md`**；**`POST /api/addresses`**；方法说明「创建地址」。
- **章节骨架**：文档中仍包含约定的 **`### Request Body (application/json)`** 与 **`### Response Body (application/json)`** 标题，与普通 DTO 用例版面一致。

## 小结

本用例覆盖 **AST/字段模型对 `record` 的分支**，以及 **Record Javadoc 用 `@param` 描述分量** 这一常见写法；与 Java 8 IT 用的 class + Lombok 路径形成互补。

# doc-analyzer / request-mapping-params

## 本用例在验证什么

检查 **`@RequestMapping` / `@GetMapping` 上的 `params = { "k=v" }`** 这类 **Spring 映射条件** 是否会并入文档中的 **对外 URL**（以查询串形式固定下来），并与类级、方法级路径 **`value`** 正确拼接。

## 功能点与分支关注点（逐条）

- **类级条件参数**：控制器上 **`params = {"module=system"}`** 与路径 **`/api/config`** 组合后，文档中的完整 URL 需带 **`?module=system`**。
- **方法级追加**：方法上再声明 **`params = {"action=read"}`** 时，应以 **`&`** 拼到已有查询串后，形成 **`module=system&action=read`**（首段用 **`?`**，后续用 **`&`**）。
- **最终形态断言**：文档中出现形如 **`GET /api/config?module=system&action=read`** 的整串（动词 + 路径 + 全部条件参数）。
- **响应 DTO 仍解析**：**`ConfigResp`** 的 **`configKey` / `configValue`** 及中文注释（配置键、配置值）出现在 Response Body 语境中。
- **产物**：单文件 **`配置管理.md`**；方法说明「查询配置」。

## 小结

本用例专门覆盖 **「仅通过 `params` 区分读场景」** 的映射写法在文档里的可见性，避免文档只印路径而丢掉 **必选查询条件** 语义。

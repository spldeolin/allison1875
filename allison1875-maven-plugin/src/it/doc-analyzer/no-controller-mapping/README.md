# doc-analyzer / no-controller-mapping

## 本用例在验证什么

当 **`@RestController` 上没有任何类级 `@RequestMapping`**（无前缀路径、无类级 HTTP 动词）时，doc-analyzer 仍应正确识别各 handler，并把 **方法级映射上的路径** 直接当作 **完整对外 URL** 写进文档，而不是误拼成空路径或重复斜杠。

## 功能点与分支关注点（逐条）

- **零类级前缀**：控制器仅保留 **`@RestController`**，每个接口只在 **`@GetMapping("/…")`** 上声明路径。
- **URL 等于方法映射**：**`GET /ping`** 与 **`GET /version`** 分别对应两个方法；文档中 **不应** 凭空出现 `/api/...` 之类未在源码中声明的前缀。
- **双 GET、同返回类型**：两个 handler 共用 **`PingResp`** 时，**Response Body** 中仍应列出 **`message` / `timestamp`** 及 Javadoc（「响应消息」「时间戳」）。
- **文档标题与文件名**：方法 Javadoc「Ping接口」「Version接口」进入正文；**单个** Markdown 文件名取自类级主题首行（本例 **`无类级RequestMapping.md`**，与类 Javadoc 标题字面一致）。

## 小结

本用例覆盖 **「只有方法级映射、没有类级 `@RequestMapping`」** 这一常见 Spring 写法下的 **路径合并边界**，避免与「类级 + 方法级」双段拼接逻辑混淆。

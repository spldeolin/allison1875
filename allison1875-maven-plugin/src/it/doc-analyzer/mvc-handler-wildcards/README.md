# doc-analyzer / mvc-handler-wildcards

## 本用例在验证什么

在配置了 **`mvcHandlerQualifierWildcards`** 后，doc-analyzer 只对 **方法全限定名命中白名单** 的 handler 生成文档；同一控制器里 **未命中的增删类接口** 应 **完全不出现在 Markdown**（含 HTTP 动词与标题文案），用于验证 **「按命名模式裁剪暴露面」** 而非全文导出。

## 功能点与分支关注点（逐条）

- **双模式并集**：`*.list*` 命中所有以 **`list`** 为方法名核心段的 handler；`*.get{ById,Detail}` 使用 **花括号列举** 合法后缀片段，命中 **`getById`**（及同类 `get…Detail` 命名若存在）；两条规则 **求或**，得到 **列表 + 详情** 两类只读接口。
- **正例**：**`listAnimals`**（「查询动物列表」、**`GET /api/animals`**）与 **`getById`**（「根据ID查询动物详情」、**`GET /api/animals/{id}`**）进入 **`动物管理.md`**；Path Param 区块存在；**`List<AnimalResp>`** 在文档中有 **列表/数组** 类表述（本例断言 **「Object Array」**）；**`AnimalResp`** 的 **`name` / `species`** 及中文注释出现在 Response Body 语境中。
- **负例（过滤生效）**：**`createAnimal`**（「创建动物」）、**`deleteAnimal`**（「删除动物」）不得出现在全文；且整篇 **不得出现 `POST`、`DELETE`**，避免仅隐藏标题却残留其它接口痕迹。
- **单文件产出**：仅 **一个** Markdown 文件，与控制器主题「动物管理」一致。

## 小结

本用例在 **单控制器四方法** 的最小集上验证 **wildcard 白名单**：**只读查询** 留下，**写操作** 整类剔除；与 `glob-regex-branches` 相比，更侧重 **业务上常见的 `list` + `getById` 命名** 与 **花括号分支** 的可读配置方式。

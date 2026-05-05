# doc-analyzer / jakarta-validation

## 本用例在验证什么

在 **Spring 6 / Jakarta EE** 依赖与源码中使用 **`jakarta.validation.constraints.*`** 时，doc-analyzer 能否像对待 `javax.validation` 一样，把常见约束 **翻译成 Markdown 里的中文校验说明**；并确认配置 **`enableJavaxMoveToJakarta: true`** 与 **Java 17** 子工程场景下整条链路可跑通。

## 功能点与分支关注点（逐条）

- **运行环境**：子工程 **Java 17**，依赖 **Spring WebMVC 6.x**、**Jakarta Validation API**；`.allison1875.yml` 中 **`javaVersion: "17"`** 与 **`enableJavaxMoveToJakarta: true`** 与之一致（该 IT 通常归入需 **JDK 17+** 的 invoker profile）。
- **字符串组合校验**：**`@NotBlank`** 与 **`@Size(min, max)`** 同时标注时，文档需出现 **非空/长度上下限** 类表述（本例 **2～50** 与字段 **`memberName` /「会员名称」**）。
- **整数范围 + 非空**：**`@NotNull`** 与 **`@Min` / `@Max`** 组合（本例等级 **1～5**）需在文档中分别体现。
- **时间语义 `@Past`**：**`Date`** 上标注「必须是过去」类说明，字段 **`birthday`** 与「出生日期」注释进入 Request Body 描述。
- **响应体照常生成**：**`MemberResp`** 的 **`id`**、**`level`** 及「会员等级」等仍出现在 Response Body 相关段落，与请求侧校验互不干扰。
- **产物与路由**：单文件 **`会员管理.md`**；**`POST /api/members`**；方法说明「创建会员」。

## 小结

本用例与 Java 8 IT 里的 **`javax.validation`** 场景对偶，专门覆盖 **Jakarta 包名下的同一批约束注解** 以及 **`@Past`** 的文档化，避免 Spring Boot 3 升级后校验说明断档。

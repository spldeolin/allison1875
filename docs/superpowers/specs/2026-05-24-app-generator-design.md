# App Generator Design Spec

## Overview

app-generator 基于 AppDef YAML DSL 生成一份前后端源码，源码构建后可直接得到一个 fullstack jar 运行。前端为 Vue3 + Naive UI，后端为 Spring Boot 2.7 + MyBatis。

## DSL 结构

用户提供单文件 `app.yml`，路径通过 allison1875 config 指定。

```yaml
namespace: com.example.demo
name: StudentManagement
title: 学生管理系统

menus:
  - group: 学生管理
    icon: PersonOutline
    order: 1
    form:
      name: StudentBasicInfo
      title: 学生基本信息
      desc: 学生身份信息管理
      items:
        - type: text
          name: studentName
          title: 学生姓名
          isNonVoid: true
          canInputOnInit: true
          canInputOnEdit: true
          maxLength: 50
      indices:
        - itemNames: [studentId]
          isUnique: true

  - group: 教务管理
    icon: CalendarOutline
    order: 2
    form:
      name: CourseSchedule
      title: 课程安排
      items:
        - type: text
          name: courseName
          title: 课程名称
          isNonVoid: true
          canInputOnInit: true
          canInputOnEdit: true
          maxLength: 100
```

## Java 模型

### AppDef

```java
public class AppDef {
    @NotEmpty String namespace;   // 小写字母、数字、点号
    @UpperCamel String name;      // UpperCamel 英语单词
    @NotEmpty String title;       // 应用标题
    @NotEmpty List<@NotNull MenuDef> menus;
}
```

### MenuDef

```java
public class MenuDef {
    String group;     // 菜单分组名
    String icon;      // ionicons5 图标名
    String order;     // 排序权重
    @NotNull @Valid FormDef form;  // 关联的表单定义（来自 form-generator 模块）
}
```

## 生成流程

AppGenerator.process() 执行以下步骤：

1. **解析 DSL**：读取 app.yml，反序列化为 AppDef，执行 JSR303 校验
2. **创建输出目录**：`{outputDir}/{name}/`
3. **生成后端**：
   - 复制 backend-skeleton → `{outputDir}/{name}/{name}-backend/`
   - 全局替换占位符（`__NAMESPACE__`、`__NAMESPACE_PATH__`、`__APP_NAME__`、`__APP_TITLE__`）
   - 从 AppDef.menus 中提取所有 FormDef
   - 构造 form-generator 所需的 Config + DomainContext，指向输出的后端目录
   - 调用 FormGenerator.process() 生成 CRUD 代码（Entity、Controller、DDL、Mapper 等）
4. **生成前端**：
   - 复制 frontend-skeleton → `{outputDir}/{name}/{name}-frontend/`
   - 将 AppDef 序列化为 `app.json`，写入前端 `src/app.json`
   - 删除示例文件（如骨架中的示例 app.json）
5. **生成 README.md**：写入构建、运行方式和原始 DSL 内容
6. **输出完成日志**

## 输出目录结构

```
{outputDir}/{name}/
├── README.md
├── {name}-backend/
│   ├── pom.xml (finalName: {name}-fullstack)
│   ├── sql/ddl.sql
│   ├── src/main/java/{namespace_path}/
│   │   ├── Application.java
│   │   ├── config/
│   │   ├── webmvc/
│   │   ├── mybatis/
│   │   ├── trace/
│   │   ├── id/
│   │   ├── util/
│   │   ├── common/
│   │   ├── controller/    (form-generator 生成)
│   │   ├── entity/        (form-generator 生成)
│   │   ├── mapper/        (form-generator 生成)
│   │   └── design/        (form-generator 生成)
│   └── src/main/resources/
│       ├── application.yml
│       ├── logback-spring.xml
│       ├── static/        (前端 build 产物放这里)
│       └── mapper/*.xml   (form-generator 生成)
└── {name}-frontend/
    ├── package.json
    ├── vite.config.ts
    ├── src/
    │   ├── app.json       (app-generator 生成)
    │   ├── App.vue
    │   ├── main.ts
    │   ├── router/
    │   ├── layouts/
    │   ├── views/
    │   ├── core/
    │   ├── stores/
    │   ├── utils/
    │   └── schema/
    └── tests/
```

## backend-skeleton 设计

基于 satisficing 项目代码复制，单模块结构，完全独立。

### 技术栈

- Spring Boot 2.7.18
- Spring Cloud 2021.0.9 (Sleuth)
- Jasypt
- MyBatis + PageHelper
- Jackson + JSR310
- MySQL Connector
- Guava, Commons Lang3, Commons Collections4, Commons IO
- Logstash Logback Encoder

### 包结构

```
{namespace}/
├── Application.java
├── config/
│   ├── JacksonConfig.java
│   ├── WebMvcConfig.java
│   └── ThreadPoolConfig.java
├── webmvc/
│   └── GlobalExceptionAdvice.java
├── mybatis/
│   ├── EnumTypeHandlerEx.java
│   └── EnumAncestorTypeHandler.java
├── trace/
│   ├── HttpBodyReportFilter.java
│   ├── HttpBodyReportFilterExclusion.java
│   └── DefaultHttpBodyReportExclusion.java
├── id/
│   └── SnowFlake.java
├── util/
│   ├── JsonUtils.java
│   ├── PageUtils.java
│   └── TimeUtils.java
└── common/
    ├── RequestResult.java
    ├── BaseEnum.java
    └── BizException.java
```

### 占位符

| 占位符 | 替换值 |
|--------|--------|
| `__NAMESPACE__` | AppDef.namespace (如 `com.example.demo`) |
| `__NAMESPACE_PATH__` | namespace 转路径 (如 `com/example/demo`) |
| `__APP_NAME__` | AppDef.name (如 `StudentManagement`) |
| `__APP_TITLE__` | AppDef.title (如 `学生管理系统`) |

### 静态资源配置

backend application.yml 中配置 Spring Boot 静态资源 serve：

```yaml
spring:
  web:
    resources:
      static-locations: classpath:/static/
  mvc:
    throw-exception-if-no-handler-found: false
```

确保前端 build 产物放入 `src/main/resources/static/` 后可被正确访问。

## frontend-skeleton 改造

### 删除

- `plugins/vite-plugin-form-dsl.ts`
- `src/dsl/` 目录
- `docs/` 目录（迁移到 allison1875 根目录）
- `package.json` 中 `js-yaml` 和 `@types/js-yaml` 依赖
- `vite.config.ts` 中 form-dsl plugin 引用
- 相关测试 `tests/plugins/vite-plugin-form-dsl.test.ts`

### 新增/修改

- `src/app.json`：示例 AppDef JSON（开发用）
- `src/schema/types.ts`：新增 `AppDef`、`MenuDef` 类型；`FormDef` 去掉 `group`/`icon`/`order`
- `src/router/index.ts`：从 `app.json` 导入 AppDef，遍历 menus 生成路由
- `src/layouts/DashboardLayout.vue`：侧边栏标题从 AppDef.title 读取
- `vite.config.ts`：简化为纯 Vue plugin

### app.json 结构

```json
{
  "namespace": "com.example.demo",
  "name": "StudentManagement",
  "title": "学生管理系统",
  "menus": [
    {
      "group": "学生管理",
      "icon": "PersonOutline",
      "order": 1,
      "form": {
        "name": "StudentBasicInfo",
        "title": "学生基本信息",
        "items": [...],
        "indices": [...]
      }
    }
  ]
}
```

## README.md 生成

生成物根目录下的 README.md 包含：

1. **构建步骤**：
   ```bash
   cd {name}-frontend
   npm install
   npm run build
   cp -r dist/* ../{name}-backend/src/main/resources/static/
   cd ../{name}-backend
   mvn package
   ```
2. **运行方式**：
   ```bash
   java -jar {name}-backend/target/{name}-fullstack.jar
   ```
3. **原始 DSL**：附上完整的 app.yml 内容

## superpowers 目录迁移

将 `app-generator/src/main/resources/frontend-skeleton/docs/superpowers/` 下的所有内容移动到 `allison1875/docs/superpowers/`：
- `specs/2026-05-23-form-web-design.md`
- `plans/2026-05-23-form-web-implementation.md`

移动后删除 `frontend-skeleton/docs/` 目录。

## 不在范围

- 用户管理、权限管理、登录登出（前端 auth store 和 Login.vue 保留为 mock 实现，后续专项需求）
- app-generator 的 Maven plugin 封装
- 生成产物的 CI/CD 集成

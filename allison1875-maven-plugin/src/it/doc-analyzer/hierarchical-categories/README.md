# hierarchical-categories 集成测试

## 概述

验证 `package-info.java` 中的层级分类 Javadoc → `hierarchicalCategories` 非空 → markdown 文件生成在层级子目录中。

## 覆盖的功能

### 1. analyzeHierarchicalCategories（MvcHandlerAnalyzerServiceImpl）

- `com.example.controller.admin` 包的 `package-info.java` 包含 Javadoc "后台管理模块"
- `hierarchicalCategories` 列表非空

### 2. MarkdownServiceImpl.flushToMarkdown 层级目录生成

- markdown 文件生成在 `api-docs/后台管理模块/` 子目录下

### 3. Endpoint 基本信息

- handler: `查询系统设置`
- URL: `GET /api/admin/settings`
- md 文件：`api-docs/后台管理模块/系统设置.md`
- api-docs 根目录下**不直接放置** md 文件，全部在层级子目录中

### 4. Response Body 字段

- key（设置键）、value（设置值）

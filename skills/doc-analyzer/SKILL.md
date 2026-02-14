---
name: doc-analyzer
description: 分析 Spring MVC Controller 并自动生成 API 文档。当用户要求"生成 API 文档"、"导出接口文档"、"同步文档到 YApi"、"生成 Markdown 文档"、"分析 Controller 接口"时使用。支持输出到 YApi、Markdown、Showdoc 或 DSL 格式。
---

# Doc Analyzer（文档分析器）

当用户需要自动生成 API 文档时使用。分析 Spring MVC Controller 中的 handler 方法，提取 URL、HTTP 方法、请求/响应结构、Javadoc 注释等信息，输出为结构化文档。

## 前置条件

确保用户项目的 `pom.xml` 包含 Allison1875 Maven 插件配置：

```xml
<plugin>
    <groupId>com.spldeolin.allison1875</groupId>
    <artifactId>allison1875-maven-plugin</artifactId>
    <version>${allison1875.version}</version>
    <configuration>
        <common>
            <basePackage>用户项目的根包</basePackage>
            <author>作者名</author>
        </common>
        <docAnalyzer>
            <!-- 文档输出目标：MARKDOWN, YAPI, SHOWDOC, DSL -->
            <flushTo>MARKDOWN</flushTo>
            <!-- 其他配置见下方说明 -->
        </docAnalyzer>
    </configuration>
</plugin>
```

## 执行步骤

### 第一步：配置文档输出目标

在 `pom.xml` 的 `<docAnalyzer>` 中配置：

**基础配置：**

| 配置项 | 默认值 | 说明 |
|---|---|---|
| `flushTo` | `MARKDOWN` | 文档输出目标，可选：`MARKDOWN`、`YAPI`、`SHOWDOC`、`DSL` |
| `globalUrlPrefix` | `""` | 全局 URL 前缀 |
| `dependencyDirsOrJavaFilePath` | `[]` | handler 方法签名所依赖的外部项目目录 |
| `mvcHandlerQualifierWildcards` | `null` | 方法全限定名通配符，只分析匹配的 handler |

**输出到 Markdown：**

| 配置项 | 默认值 | 说明 |
|---|---|---|
| `markdownDir` | `api-docs` | Markdown 文件输出目录 |
| `singleEndpointPerMarkdown` | `false` | 每个 Endpoint 输出为单独的 Markdown 文件 |
| `enableCurl` | `false` | 是否输出 cURL 命令示例 |
| `enableResponseBodySample` | `false` | 是否输出 Response Body 示例 |

**输出到 YApi：**

| 配置项 | 说明 |
|---|---|
| `yapiUrl` | YApi 服务器 URL |
| `yapiToken` | YApi 项目的 TOKEN |

**输出到 Showdoc：**

| 配置项 | 默认值 | 说明 |
|---|---|---|
| `showdocUrl` | - | Showdoc 开放 API URL |
| `showdocApiKey` | - | Showdoc API Key |
| `showdocApiToken` | - | Showdoc API Token |
| `showdocBaseCatName` | `doc-analyzer` | 文档基础目录名 |

**输出到 DSL：**

| 配置项 | 默认值 | 说明 |
|---|---|---|
| `dslDir` | `api-dsls` | DSL 文件输出目录 |

### 第二步：执行 Maven 命令

```bash
mvn allison1875:doc-analyzer
```

### 输出结果

根据配置的 `flushTo` 输出：

1. **MARKDOWN**：在 `markdownDir` 目录生成 Markdown 格式的 API 文档
2. **YAPI**：同步 API 文档到 YApi 平台
3. **SHOWDOC**：同步 API 文档到 Showdoc 平台
4. **DSL**：在 `dslDir` 目录生成 DSL 格式文件

## 文档内容

分析并输出的信息包括：

- **基本信息**：分类、描述、是否过时、作者、源码位置
- **URL 信息**：HTTP 方法、URL 路径
- **Path 参数**：路径变量
- **Query 参数**：查询字符串参数
- **Request Body**：请求体 JSON Schema
- **Response Body**：响应体 JSON Schema

## 配置示例

**输出到本地 Markdown：**

```xml
<docAnalyzer>
    <flushTo>MARKDOWN</flushTo>
    <markdownDir>docs/api</markdownDir>
    <singleEndpointPerMarkdown>true</singleEndpointPerMarkdown>
    <enableCurl>true</enableCurl>
    <enableResponseBodySample>true</enableResponseBodySample>
</docAnalyzer>
```

**同步到 YApi：**

```xml
<docAnalyzer>
    <flushTo>YAPI</flushTo>
    <yapiUrl>http://yapi.example.com</yapiUrl>
    <yapiToken>your-project-token</yapiToken>
</docAnalyzer>
```

**只分析特定 Controller：**

```xml
<docAnalyzer>
    <flushTo>MARKDOWN</flushTo>
    <mvcHandlerQualifierWildcards>
        <param>com.example.controller.UserController.*</param>
        <param>com.example.controller.OrderController.create*</param>
    </mvcHandlerQualifierWildcards>
</docAnalyzer>
```

## 故障排查

| 问题 | 解决方案 |
|---|---|
| 命令失败提示 "plugin not found" | Allison1875 未发布到 Maven Central，需要用户克隆仓库并运行 `mvn install -DskipTests` |
| 没有检测到 MVC Handler | 确保 Controller 类有 `@RestController` 或 `@Controller` 注解，方法有 `@RequestMapping` 等注解 |
| YApi 同步失败 | 检查 `yapiUrl` 和 `yapiToken` 是否正确 |
| 文档内容不完整 | 确保 DTO 字段有 Javadoc 注释，这些会被提取为字段描述 |

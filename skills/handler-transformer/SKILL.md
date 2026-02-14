---
name: handler-transformer
description: 在 Controller 中创建新的 HTTP API 接口。当用户要求"添加一个接口"、"创建 API"、"添加 handler"、"生成 Controller 方法"或描述请求/响应结构时使用。自动生成 Controller 方法、Service 接口/实现、请求/响应 DTO。
---

# Handler Transformer（处理器转换器）

当用户需要创建新的 HTTP API 接口时，使用 handler-transformer 通过 DSL 生成 Controller + Service + DTO 代码。

## 前置条件

确保用户项目的 `pom.xml` 包含 Allison1875 Maven 插件配置。如果缺失，先添加：

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
        <handlerTransformer>
            <pageTypeQualifier>分页包装类的全限定名</pageTypeQualifier>
            <enableOneService>false</enableOneService>
        </handlerTransformer>
    </configuration>
</plugin>
```

还需要添加 support 依赖：

```xml
<dependency>
    <groupId>com.spldeolin.allison1875</groupId>
    <artifactId>allison1875-support</artifactId>
    <version>${allison1875.version}</version>
</dependency>
```

## 执行步骤

### 第一步：在 Controller 中编写 DSL

在现有的 `@RestController` 或 `@Controller` 类中插入实例初始化代码块 `{ ... }`。

**DSL 模板：**

```java
{
    String handler = "方法名", desc = "接口描述";

    class req {
        /**
         * 字段说明（会成为 Javadoc）
         */
        @NotNull  // JSR-303 注解会保留
        String fieldName;

        // 嵌套类会生成嵌套 DTO
        class nestedObject {
            String nestedField;
        }
    }

    // @P 表示分页返回，@L 标注在嵌套类上表示 List 字段
    @P
    class resp {
        String userId;

        @L
        class items {
            String itemName;
        }
    }
}
```

**DSL 规则速查：**

| 元素 | 说明 |
|---|---|
| `String handler = "methodName"` 或 `String h = "methodName"` | 方法名和 URL 路径 |
| `String desc = "描述"` 或 `String d = "描述"` | JavaDoc 描述 |
| `class req { ... }` | 请求体 DTO 字段定义 |
| `class resp { ... }` | 响应 DTO 字段定义 |
| `@P` 标注在 resp 上 | 响应包装为分页类型 |
| `@L` 标注在嵌套 class 上 | 字段变为 `List<XxxDTO>` |
| 字段默认值如 `int pageSize = 20` | 保留在生成的 DTO 中 |
| 字段上的 Javadoc 注释 | 保留为 DTO 字段的 Javadoc |

**完整示例：**

```java
@RestController
@RequestMapping("/users")
public class UserController {

    {
        String handler = "listUsersAsPage", desc = "获取学生分页列表";

        class req {
            /**
             * 学校名称，模糊匹配，非必填
             */
            String schoolName;

            /**
             * 地区
             */
            class address {
                String province;
                String city;
            }

            @NotNull
            int pageNum;

            @NotNull
            int pageSize = 20;
        }

        @P
        class resp {
            String userId;
            String userName;

            @L
            class address {
                String province;
                String city;
            }
        }
    }

}
```

### 第二步：执行 Maven 命令

```bash
mvn allison1875:handler-transformer
```

### 生成结果

1. **Controller 方法**（DSL 代码块被替换）：
   ```java
   @PostMapping("listUsersAsPage")
   public ApiBaseResult<PageResult<ListUsersAsPageResp>> listUsersAsPage(
           @RequestBody @Valid ListUsersAsPageReq req) {
       return ApiBaseResult.successRet(userService.listUsersAsPage(req));
   }
   ```

2. **请求 DTO**：`ListUsersAsPageReq.java`
3. **响应 DTO**：`ListUsersAsPageResp.java`
4. **嵌套 DTO**：`AddressDTO.java`（如有嵌套类）
5. **Service 接口方法**
6. **Service 实现方法**（返回 null，用户填充业务逻辑）

## 故障排查

| 问题 | 解决方案 |
|---|---|
| 命令失败提示 "plugin not found" | Allison1875 未发布到 Maven Central，需要用户克隆仓库并运行 `mvn install -DskipTests` |
| DSL 代码块没有被识别 | 确保是**实例初始化代码块**（不是 static 块），且类有 `@RestController` 或 `@Controller` 注解 |
| 生成代码使用 javax.validation | 在 `<common>` 中设置 `<enableJavaxMoveToJakarta>true</enableJavaxMoveToJakarta>` |

# 编写 .allison1875.yml 方法论

## 1. 分析顺序

```
pom.xml → 源码目录结构 → Controller → application.properties → Mapper XML → Config.java
```

**按此顺序推理，先定骨架再填细节。**

### 1.1 从 pom.xml 确定全局参数

| 关注点                       | 推理目标                                          |
|---------------------------|-----------------------------------------------|
| `<maven.compiler.source>` | → `javaVersion`                               |
| 子模块列表                     | → 识别 api/application/domain/infrastructure 分层 |
| lombok 依赖                 | → `isDataModelWithoutLombok`                  |

### 1.2 从源码目录结构确定 domains

**核心思路**：沿着 `src/main/java` 的包树，识别各层的包名。

| 要找的东西               | 怎么找                                    | 映射到                                |
|---------------------|----------------------------------------|------------------------------------|
| `@RestController` 类 | grep `@RestController`                 | `controllerPackage`                |
| req/resp DTO        | 按包名 `dto`、`req`、`resp`                 | `reqDTOPackage` / `respDTOPackage` |
| 枚举类                 | 包名含 `enums`                            | `enumPackage`                      |
| Service 接口          | 包名含 `service`                          | `servicePackage`                   |
| ServiceImpl         | 包名含 `service.impl` 或 service 子包下的 impl | `serviceImplPackage`               |
| Mapper 接口           | 包名含 `mapper`                           | `mapperPackage`                    |
| Entity/Model        | 包名含 `entity`/`module`/`model`          | `entityPackage`                    |
| Mapper XML          | `src/main/resources/mapper` 下          | `mapperXmlDirs`                    |

> **注意**：`*Module` 字段必须用**绝对路径**，不是包名。

### 1.3 从 Controller 确定 codeSnippet

grep Controller 文件中的 import 语句：

```bash
grep "import.*ApiBaseResult\|import.*PageResult\|import.*RequestResult" **/*.java
```

- 统一返回类 → `requestResultQualifier` + `requestResultTypeDeclaration`
- 分页类 → `pageTypeQualifier` + `constructPageResult`
- `jakarta.*` vs `javax.*` → `enableJavaxMoveToJakarta`

### 1.4 从 application.properties 推理持久层

| properties 键                   | 映射到        |
|--------------------------------|------------|
| `spring.datasource.*.jdbc-url` | `jdbcUrl`  |
| `spring.datasource.*.username` | `userName` |
| `spring.datasource.*.password` | `password` |
| JDBC URL 中 `/dbname?`          | `schema`   |

### 1.5 从 Mapper XML 推理逻辑删除

搜索 Mapper XML 中的 `delete_flag` / `is_deleted` 模式：

```bash
grep "delete_flag\|is_deleted" mapper/*.xml | head -20
```

观察 `SET delete_flag = 1` 和 `AND delete_flag = 0` → 得出 `deletedSql` / `notDeletedSql`。

## 2. 常见陷阱

| 陷阱                                       | 说明                                             | 解法                                                    |
|------------------------------------------|------------------------------------------------|-------------------------------------------------------|
| **Module 路径写成相对路径**                      | allison1875 DomainConfig 的 `*Module` 字段要求绝对路径  | 始终用完整的文件系统路径                                          |
| **DTO 分散在多个子包**                          | 项目按子领域拆分 DTO（如 `dto/benchmark/req`），但配置只接受一个包名 | 统一配为上层包（如 `application.dto`）                          |
| **serviceImplPackage 不一定是 service.impl** | 有些项目 impl 分布在各 service 子包下                     | 如果 impl 没有统一包，serviceImplPackage 可与 servicePackage 相同 |
| **entityPackage 不一定叫 entity**            | 部分项目用的是 `module` 包名                            | 实际看项目结构，不要想当然                                         |
| **Config 有默认值 ≠ 可以不写**                   | 显式写出所有配置项更利于维护和 review                         | 即便是默认值也写出来                                            |
| **mapperXmlDirs 默认是 `mapper/`**          | 项目可能在 `mapper/mysql/` 子目录                      | 检查实际 resources 目录结构                                   |

## 3. 校验清单

完成编写后，逐项检查：

- [ ] `domains` 至少一个，`name` 不为空
- [ ] 所有 `*Module` 字段是绝对路径且目录存在
- [ ] 所有 `*Package` 字段与实际源码包名一致
- [ ] `enableJavaxMoveToJakarta` 与项目实际 import 一致
- [ ] `javaVersion` 与 pom 的 compiler source 一致
- [ ] `codeSnippet.constructPageResult` 和 `constructEmptyPageResult` 不为空（Config 校验必填）
- [ ] `jdbcUrl` 可连通目标数据库（如果配了 persistence-generator）
- [ ] `deletedSql` / `notDeletedSql` 与 Mapper XML 中的写法一致
- [ ] `mapperXmlDirs` 路径相对于 persistenceModule 的 basedir

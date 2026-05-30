---
name: generate-form
description: Use allison1875 form-generator to generate form code for a Spring Boot project. If the project has not integrated allison1875 (no pom dependency or no .allison1875.yml), apply the allison1875-integration skill first. Then infer or ask for form requirements, write forms.yml DSL, and run mvn allison1875:form-generator (outside sandbox). Use when the user asks to generate forms with allison1875, use form-generator, create forms.yml, or generate form CRUD/API code.
---

# 使用 Allison1875 Form-Generator 生成表单

指导大模型为现代 Spring Boot 工程使用 allison1875 的 form-generator 生成表单代码。流程：先确保已集成 allison1875 → 推断/询问表单需求 → 编写 forms.yml DSL → 执行 `mvn allison1875:form-generator`（须在沙箱外执行以落盘）。

## 一、前置条件：确保已集成 Allison1875

若满足以下**任一**情况，视为**未集成**，必须先按 **allison1875-integration** 技能完成集成后再继续：

- `pom.xml` 中未包含 `allison1875-support` 依赖；
- `pom.xml` 的 `<build><plugins>` 中未包含 `allison1875-maven-plugin`；

## 二、推断或询问表单需求

通过以下方式确定要生成的表单：

1. **分析**：从用户描述、现有 Controller/Entity、需求文档、接口命名等推断表单名称、字段及类型。
2. **询问**：当存在歧义或信息不足时主动询问，例如：
   - 表单名称/标题（英文 UpperCamel 名、中文标题）；
   - 每个字段：名称（lowerCamel）、标题、是否必填、类型（text/number/select/multiSelect/time/onOff/secret）；
   - 对 select/multiSelect：选项列表（code + title）；
   - 对 text：是否多行、最大长度、正则；
   - 对 number：是否允许小数；
   - 对 time：日期 / 时间 / 日期时间；
   - 需要唯一或组合索引的字段。

输出前可简要列出「将要生成的表单与字段」，但无需用户确认，直接写 DSL 与执行生成。

## 三、编写 forms.yml 文件

表单需求确认完毕后，需转化为一种DSL来描述表单的定义，并以 YAML 的形式写入 `forms.yml` 文件。

YAML 的形式如下，通过一个覆盖常见类型和索引场景的示例来说明： 

- 根结构：YAML 根节点是一个数组（`List<FormDef>`），不要使用顶层 `forms:` 包装。
- **覆盖写入**：写入 `forms.yml` 时，**不论该文件是否已经存在，均以新内容完整覆盖**；禁止追加到原有内容之后，禁止保留原有内容的任何部分。

```yaml
# 一个典型的“员工信息”表单，演示各种类型表单字段的声明方式，以及唯一索引和普通索引的声明方式

- name: EmployeeBasic           # 表单名称，UpperCamel，不以 Form 结尾
  title: 员工基础信息             # 表单标题，对用户可见
  desc: 员工基础信息录入与维护      # 可选的表单描述
  
  # 表单字段列表，至少1个元素，且每个item.name必须唯一。不要在 items 中定义主键字段（如 xxxCode）或审计字段（如 createdAt、updatedAt），这些由 form-generator 自动添加。
  items:

    # 文本字段，单行，带最大长度与正则校验；不写 canInputOnInit/canInputOnEdit 即默认 userInput
    - type: text
      name: employeeName        # 字段名，lowerCamel
      title: 员工姓名
      isNonVoid: true           # 是否非空，必须显式 true/false
      maxLength: 50             # 可选，默认 255；不超过 65535
      regex: "^[\\u4e00-\\u9fa5A-Za-z\\s]+$"  # 可选，正则约束
      # canInputOnInit / canInputOnEdit：不写时默认为 userInput（用户输入）。可选 doNot（不初始化/不可编辑）、todo（生成 TODO 由开发者实现）。

    # 入职日期：创建时用户填写，保存后不可再改（canInputOnEdit: false）
    - type: time
      name: hireDate
      title: 入职日期
      isNonVoid: true
      format: date
      canInputOnInit: true
      canInputOnEdit: false

    # 内部备注：不对外展示，生成 TODO 由开发者自行实现逻辑（canInputOnInit/canInputOnEdit: false）
    - type: text
      name: internalNote
      title: 内部备注
      isNonVoid: false
      maxLength: 500
      canInputOnInit: false
      canInputOnEdit: false

    # 文本字段，多行备注
    - type: text
      name: remark
      title: 备注
      isNonVoid: false
      isMultilineOrRich: true   # 可选，默认 false
      maxLength: 2000

    # 数字字段，整型
    - type: number
      name: age
      title: 年龄
      isNonVoid: true
      canBeDecimal: false       # 可选，默认 false；是否允许小数

    # 数字字段，允许小数（如薪资）
    - type: number
      name: salary
      title: 薪资
      isNonVoid: true
      canBeDecimal: true

    # 单选字段，必须提供非空 options 列表
    - type: select
      name: department
      title: 部门
      isNonVoid: true
      options:
        - code: engineering     # code 唯一标识，建议英文，长度 <= 64
          title: 工程部
        - code: hr
          title: 人力资源部
        - code: marketing
          title: 市场部

    # 多选字段，不能出现在索引中
    - type: multiSelect
      name: skills
      title: 技能标签
      isNonVoid: false
      options:
        - code: java
          title: Java
        - code: python
          title: Python
        - code: projectManagement
          title: 项目管理

    # 布尔开关字段
    - type: onOff
      name: isActive
      title: 是否在职
      isNonVoid: true

    # 密文字段，表示密码/密钥；存库加密，不返回原文，一般只支持重置
    - type: secret
      name: loginPassword
      title: 登录密码
      isNonVoid: true

  indices:
    # 唯一索引：约束工号唯一
    - itemNames: [ employeeName ]   # 只能引用 items 中已存在的 name
      isUnique: false               # 可选，默认 false

    # 组合普通索引：按部门 + 是否在职查询
    - itemNames: [ department, isActive ]
      isUnique: false

# 另一个表单示例：学生选课，演示多个 FormDef 组成的列表结构

- name: CourseSelection
  title: 学生选课
  desc: 学生为学期进行课程选择

  items:
    - type: number
      name: studentId
      title: 学号
      isNonVoid: true

    - type: select
      name: term
      title: 学期
      isNonVoid: true
      options:
        - code: spring2025
          title: 2025 春季学期
        - code: autumn2025
          title: 2025 秋季学期

    - type: multiSelect
      name: courseCodes
      title: 课程编号列表
      isNonVoid: true
      options:
        - code: cs101
          title: 计算机基础
        - code: math201
          title: 高等数学

  indices:
    # 唯一索引：同一学号 + 学期 只能有一条选课记录
    - itemNames: [ studentId, term ]
      isUnique: true
```

## 四、执行 form-generator

1. **命令**：在项目根目录（与 `pom.xml` 同级）执行`mvn allison1875:form-generator`
2. **沙箱与落盘**：该命令会生成并写入 Java/资源文件到本地。**必须在沙箱外执行**：调用执行命令时需请求 `required_permissions: ['all']`，否则生成文件可能无法落盘。执行前可提示用户「将使用 Maven 生成代码并写入项目文件」。
3. **Java 版本**：若项目使用 Java 21，执行前可设置 `JAVA_HOME=$(/usr/libexec/java_home -v 21)`（按用户规则）。
4. 执行后根据控制台输出确认是否成功；若有校验或编译错误，根据报错修正 forms.yml 或配置后重跑。

## 五、流程小结

1. 检查集成 → 未集成则先应用 allison1875-integration SKILL
2. 推断/询问 → 确定表单名、字段、类型、选项、索引
3. 编写/更新 forms.yml → 根节点为 YAML 数组，遵守上述 DSL 与约束
4. 在项目根、沙箱外执行 mvn allison1875:form-generator
5. 根据输出验证生成结果
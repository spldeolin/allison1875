---
name: generate-form
description: 指导 Agent 根据用户需求生成符合 allison1875 form-generator 协议的 form DSL（forms.yml），并在每次创建/修改后运行语法检查命令直至通过，最后执行 form-generator 生成代码。当用户要求用 allison1875 生成表单、使用 form-generator、编写或修改 forms.yml、生成表单 CRUD/API 代码时使用。
---

# 生成 Allison1875 Form DSL

把用户的表单需求转化为一份合法的 **form DSL**（`forms.yml`），用语法检查命令验证，再执行 form-generator 生成代码。本技能只负责 DSL 的编写与校验，不涉及 form-generator 内部实现。

## 步骤

1. **梳理需求**：从用户描述、现有 Controller/Entity、接口命名等推断表单名、字段、类型、选项、索引。信息不足或有歧义时**主动询问**（如字段类型、select 选项、哪些字段唯一、time 用日期还是日期时间）。
2. **写入 forms.yml**：参照下方[全字段示例](#全字段-dsl-示例)编写（每个字段的含义、约束、默认值都写在注释里）。**始终用新内容完整覆盖整个文件**，禁止追加或保留旧内容。默认路径为项目根目录的 `forms.yml`（实际以 `.allison1875.yml` 的 `dslPath` 为准）。
3. **运行语法检查，直至通过**：
   ```bash
   <VALIDATE_COMMAND>   # ⚠️ 待补：form-generator 后续提供的 DSL 语法检查命令
   ```
   不通过时，按报错（形如 `forms[i].items[j].name ...`）定位字段并修复，**重跑直到通过**，不得跳过。
4. **执行 form-generator 生成代码**（在项目根目录）：
   ```bash
   <GENERATE_COMMAND>   # ⚠️ 待补：form-generator 执行命令
   ```
   须**在沙箱外执行**（请求 `required_permissions: ['all']`），否则文件无法落盘；Java 21 项目可先设 `JAVA_HOME=$(/usr/libexec/java_home -v 21)`。出现校验/编译错误则回到第 2 步修正并重走第 3 步。

## 全字段 DSL 示例

每个字段带注释说明含义、约束与默认值，可作为编写模板。注释中标注的规则即语法检查（源码 `FormDef.validate()`）会强制的约束，违反将导致校验失败。

```yaml
# 根节点是数组，每个 "- name:" 是一张表单；禁止顶层 forms: 包装键。

- name: EmployeeBasic          # 表单名，UpperCamel（仅字母数字、首字母大写、含小写字母、
                               #   不超过 2 个连续大写字母）；无需以 Form 结尾；不能等于 User（忽略大小写）
  title: 员工基础信息            # 表单标题，对用户可见，不能为空
  desc: 员工基础信息录入与维护    # 可选描述

  # items：业务字段列表（≥1，每个 name 唯一）。不要写主键/createdAt/updatedAt，会自动追加。
  items:

    - type: text               # 单行文本
      name: employeeName       # 字段名，lowerCamel（仅字母数字、首字母小写、无连续大写）
      title: 员工姓名            # 字段标题，不能为空
      isNonVoid: true          # 是否非空，必须显式 true/false，不可省略
      maxLength: 50            # 可选，默认 255，≤65535（仅单行文本生效）
      regex: "^[\\u4e00-\\u9fa5A-Za-z\\s]+$"  # 可选，正则约束
      # canInputOnInit/canInputOnEdit（均默认 true）：控制创建/编辑时是否允许用户输入。
      #   true/true 创建编辑都可输入（最常见）；true/false 创建后不可改；
      #   false/true 创建时不可填；false/false 始终不可输入。省略即 true/true。

    - type: text               # 多行/富文本，存库用 TEXT（maxLength 不生效）
      name: remark
      title: 备注
      isNonVoid: false
      isMultilineOrRich: true  # 可选，默认 false

    - type: time               # 时间；演示“创建时填写、保存后不可改”
      name: hireDate
      title: 入职日期
      isNonVoid: true
      format: date             # 可选，默认 dateTime；date→日期 time→时间 dateTime→日期时间
      canInputOnInit: true
      canInputOnEdit: false

    - type: number             # 整数
      name: age
      title: 年龄
      isNonVoid: true
      canBeDecimal: false      # 可选，默认 false；false→整数，true→小数

    - type: number             # 小数
      name: salary
      title: 薪资
      isNonVoid: true
      canBeDecimal: true

    - type: select             # 单选
      name: department
      title: 部门
      isNonVoid: true
      options:                 # 必填，非空
        - code: engineering    # 选项唯一标识，建议英文、≤64 字符
          title: 工程部          # 选项标题，对用户可见
        - code: hr
          title: 人力资源部

    - type: multiSelect        # 多选；不能出现在任何索引中
      name: skills
      title: 技能标签
      isNonVoid: false
      options:                 # 必填，非空
        - code: java
          title: Java
        - code: python
          title: Python

    - type: onOff              # 布尔开关，无专属字段
      name: isActive
      title: 是否在职
      isNonVoid: true

    - type: secret             # 密码/密钥，无专属字段：存库加密、不回传明文、只能重置
      name: loginPassword
      title: 登录密码
      isNonVoid: true

  # indices：可选；itemNames 非空，每个名称必须存在于 items，且不能是 multiSelect 字段
  indices:
    - itemNames: [ employeeName ]      # 唯一索引
      isUnique: true                   # 可选，默认 false
    - itemNames: [ department, isActive ]  # 组合普通索引
      isUnique: false

# 数组可包含多张表单
- name: CourseSelection
  title: 学生选课
  items:
    - type: number
      name: studentNo
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
  indices:
    - itemNames: [ studentNo, term ]   # 组合唯一索引
      isUnique: true
```

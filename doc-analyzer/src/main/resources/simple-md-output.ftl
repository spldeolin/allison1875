## 概要

`${uri}`

${description}



## 参数

<#if isRequestBodyNone>
没有参数
<#elseif isRequestBodyChaos>
参数结构无法用表格表示，只提供JSON Schema
<#else>
| name                | 描述                 | JSON类型和格式                | 校验项               |
| ------------------- | -------------------- | ---------------------------- | ------------------- |
<#list requestBodyFields as field>
| `${field.linkName}` | ${field.description} | `${field.jsonTypeAndFormat}` | ${field.validators} |
</#list>
</#if>



## 返回值

<#if isResponseBodyNone>
没有返回值
<#elseif isResponseBodyChaos>
返回值结构无法用表格表示，只提供JSON Schema
<#else>
| name                | 描述                 | JSON类型和格式                |
| ------------------- | -------------------- | ---------------------------- |
<#list responseBodyFields as field>
| `${field.linkName}` | ${field.description} | `${field.jsonTypeAndFormat}` |
</#list>
</#if>



## 开发者备注

>



## 源码

`${location}`
## 概要

${description}

`${uri}`



## 参数

<#if isRequestBodyNone>
没有参数
<#elseif isRequestBodyChaos>
参数结构无法用表格表示，只提供JSON Schema
<#else>
| name                | 描述                 | JSON类型和格式                <#if anyValidatorsExist>| 校验项             </#if> |
| ------------------- | -------------------- | ---------------------------- <#if anyValidatorsExist>| ------------------</#if> |
<#list requestBodyFields as field>
| ${field.linkName} | ${field.description} | <#list field.jsonTypeAndFormats as one>`${one}` </#list> <#if anyValidatorsExist>| ${field.validators}</#if> |
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
| ${field.linkName} | ${field.description} | <#list field.jsonTypeAndFormats as one>`${one}` </#list> |
</#list>
</#if>



## 作者

${author}



## 源码

~~~
${codeSource}
~~~
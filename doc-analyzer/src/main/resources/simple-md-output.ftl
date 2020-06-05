## 概要

- ${description}

- ${httpMethod}

- ${uri}



## 参数结构

<#if requestBodySituation == 1>
不需要参数
<#elseif requestBodySituation == 2>
参数结构过于复杂 或是 RequestBody结构存在泛化的部分，只提供JsonSchema
~~~
${requestBodyJsonSchema}
~~~
<#elseif requestBodySituation == 3>
参数结构解析失败
<#else>
<#if requestBodySituation == 5>结构的最外层是个json array</#if>
| 属性路径             | 属性名               | 描述                   | JSON类型和格式                |<#if anyValidatorsExist>校验项              |</#if>
| ------------------- | ------------------- | ---------------------- | ---------------------------- |<#if anyValidatorsExist>------------------ |</#if>
<#list requestBodyProperties as reqProp>
| ${reqProp.path}     | ${reqProp.name}     | ${reqProp.description} | ${reqProp.jsonTypeAndFormats}|<#if anyValidatorsExist>${reqProp.validators} |</#if>
</#list>
</#if>



## 返回值结构

<#if responseBodySituation == 1>
没有返回值
<#elseif responseBodySituation == 2>
返回值结构过于复杂 或是 RequestBody结构存在泛化的部分，只提供JsonSchema
~~~
${responseBodyJsonSchema}
~~~
<#elseif responseBodySituation == 3>
返回值结构解析失败
<#else>
<#if responseBodySituation == 5>结构的最外层是个json array</#if>
| 属性路径             | 属性名               | 描述                    | JSON类型和格式                 |
| ------------------- | ------------------- | ----------------------- | ----------------------------- |
<#list responseBodyProperties as respProp>
| ${respProp.path}    | ${respProp.name}    | ${respProp.description} | ${respProp.jsonTypeAndFormats}|
</#list>
</#if>



## 作者

${author}



## 源码

~~~
${sourceCode}
~~~
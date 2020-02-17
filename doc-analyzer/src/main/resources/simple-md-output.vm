## URI和描述

`${url}`

${description}



## 参数

#if(${isRequestBodyNone})
没有参数
#elseif(${isRequestBodyChaos})
参数结构无法用表格表示，只提供JSON Schema，详见**开发者备注**
#else
| name                | 描述                 | JSON类型和格式       | 校验项               |
| ------------------- | -------------------- | ------------------- | ------------------- |
#foreach($field in ${requestBodyFields})
| `${field.linkName}` | ${field.description} | `${field.jsonType}` | ${field.validators} |
#end
#end



## 返回值

#if(${isRequestBodyNone})
没有返回值
#elseif(${isRequestBodyChaos})
返回值结构无法用表格表示，只提供JSON Schema，详见**开发者备注**
#else
| name                | 描述                 | JSON类型和格式       |
| ------------------- | -------------------- | ------------------- |
#foreach($field in ${responseBodyFields})
| `${field.linkName}` | ${field.description} | `${field.jsonType}` |
#end
#end



## 开发者备注

> 　



## 源码

`${location}`
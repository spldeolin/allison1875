# json-property-access 集成测试

## 概述

验证 `@JsonProperty(access = READ_ONLY/WRITE_ONLY)` 字段过滤和 `@JsonFormat` 格式标注。

## 覆盖的功能

### 1. @JsonProperty.Access.READ_ONLY（JsgBuilderServiceImpl.hasIgnoreMarker）

- `CreateEventReq.readOnlyField` 在请求体中应被忽略（forReqOrResp=true 且 access=READ_ONLY）

### 2. @JsonProperty.Access.WRITE_ONLY（JsgBuilderServiceImpl.hasIgnoreMarker）

- `EventResp.writeOnlyForResp` 在响应体中应被忽略（forReqOrResp=false 且 access=WRITE_ONLY）

### 3. @JsonFormat（JsgBuilderServiceImpl.findPropertyDescription）

- `startTime` 字段标注 `@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")`
- 文档"其他"列应包含 `格式：yyyy-MM-dd HH:mm:ss`

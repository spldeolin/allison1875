package com.spldeolin.allison1875.docanalyzer.service;

import com.fasterxml.jackson.module.jsonSchema.JsonSchemaGenerator;
import com.google.common.collect.Table;
import com.google.inject.ImplementedBy;
import com.spldeolin.allison1875.base.ast.AstForest;
import com.spldeolin.allison1875.docanalyzer.javabean.JsonPropertyDescriptionValueDto;
import com.spldeolin.allison1875.docanalyzer.service.impl.JsgBuildServiceImpl;

/**
 * 内聚了 解析得到所有枚举、属性信息 和 生成自定义JsonSchemaGenerator对象的功能
 *
 * @author Deolin 2023-12-23
 */
@ImplementedBy(JsgBuildServiceImpl.class)
public interface JsgBuildService {

    JsonSchemaGenerator analyzeAstAndBuildJsg(Table<String, String, JsonPropertyDescriptionValueDto> jpdvs,
            boolean forReqOrResp);

    Table<String, String, JsonPropertyDescriptionValueDto> analyzeJpdvs(AstForest astForest);

}

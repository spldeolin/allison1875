package com.spldeolin.allison1875.docanalyzer.service.impl;

import java.util.Collection;
import java.util.Set;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.google.common.collect.Lists;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.common.util.JsonUtils;
import com.spldeolin.allison1875.docanalyzer.javabean.EnumCodeAndTitleDto;
import com.spldeolin.allison1875.docanalyzer.javabean.JsonPropertyDescriptionValueDto;
import com.spldeolin.allison1875.docanalyzer.service.EnumSchemaService;
import com.spldeolin.allison1875.docanalyzer.util.JsonSchemaTraverseUtils;

/**
 * @author Deolin 2020-09-12
 */
@Singleton
public class EnumSchemaServiceImpl implements EnumSchemaService {

    private static final ObjectMapper om = JsonUtils.createObjectMapper();

    @Override
    public void process(JsonSchema rootJsonSchema) {
        JsonSchemaTraverseUtils.traverse(rootJsonSchema, (propertyName, jsonSchema, parentJsonSchema, depth) -> {
            if (jsonSchema.isValueTypeSchema()) {
                Set<String> enums = jsonSchema.asValueTypeSchema().getEnums();
                if (enums != null) {
                    Collection<EnumCodeAndTitleDto> ecats = Lists.newArrayList();
                    for (String anEnum : enums) {
                        try {
                            ecats.add(om.readValue(anEnum, EnumCodeAndTitleDto.class));
                        } catch (JsonProcessingException e) {
                            // 这种情况说明anEnum没有被AnalyzeEnumConstantHandle#analyzeEnumConstant转化为JSON
                        }
                    }

                    String jpd = jsonSchema.getDescription();
                    if (jpd != null) {
                        JsonPropertyDescriptionValueDto jpdv = JsonUtils.toObject(jpd,
                                JsonPropertyDescriptionValueDto.class);
                        jpdv.setEcats(ecats);
                        jsonSchema.setDescription(JsonUtils.toJson(jpdv));
                    }

                    if (parentJsonSchema.isArraySchema()) {
                        JsonPropertyDescriptionValueDto parentJpdv = toJpdvSkipNull(parentJsonSchema.getDescription());
                        if (parentJpdv == null) {
                            parentJpdv = new JsonPropertyDescriptionValueDto();
                        }
                        parentJpdv.setEcats(ecats);
                        parentJsonSchema.setDescription(JsonUtils.toJson(parentJpdv));
                    }

                    enums.clear();
                }
            }
        });
    }

    private JsonPropertyDescriptionValueDto toJpdvSkipNull(String nullableJson) {
        if (nullableJson == null) {
            return null;
        }
        return JsonUtils.toObject(nullableJson, JsonPropertyDescriptionValueDto.class);
    }

}
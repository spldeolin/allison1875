package com.spldeolin.allison1875.docanalyzer.processor;

import java.util.Collection;
import java.util.Set;
import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.google.common.collect.Lists;
import com.spldeolin.allison1875.base.util.JsonUtils;
import com.spldeolin.allison1875.docanalyzer.dto.EnumCodeAndTitleDto;
import com.spldeolin.allison1875.docanalyzer.dto.JsonPropertyDescriptionValueDto;
import com.spldeolin.allison1875.docanalyzer.util.JsonSchemaTraverseUtils;

/**
 * @author Deolin 2020-09-12
 */
public class EnumSchemaProc {

    private final JsonSchema rootJsonSchema;

    EnumSchemaProc(JsonSchema rootJsonSchema) {
        this.rootJsonSchema = rootJsonSchema;
    }

    void process() {
        JsonSchemaTraverseUtils.traverse("", rootJsonSchema, (propertyName, jsonSchema, parentJsonSchema) -> {
            if (jsonSchema.isValueTypeSchema()) {
                Set<String> enums = jsonSchema.asValueTypeSchema().getEnums();
                if (enums != null) {
                    Collection<EnumCodeAndTitleDto> ecats = Lists.newArrayList();
                    for (String anEnum : enums) {
                        ecats.add(JsonUtils.toObject(anEnum, EnumCodeAndTitleDto.class));
                    }

                    String jpd = jsonSchema.getDescription();
                    if (jpd != null) {
                        JsonPropertyDescriptionValueDto jpdv = JsonUtils
                                .toObject(jpd, JsonPropertyDescriptionValueDto.class);
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
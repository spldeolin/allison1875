package com.spldeolin.allison1875.docanalyzer.service.impl;

import java.util.List;
import java.util.Map;
import java.util.Set;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.fasterxml.jackson.module.jsonSchema.types.ReferenceSchema;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.common.util.JsonUtils;
import com.spldeolin.allison1875.docanalyzer.javabean.AnalyzeEnumConstantRetval;
import com.spldeolin.allison1875.docanalyzer.javabean.JsonPropertyDescriptionValueDto;
import com.spldeolin.allison1875.docanalyzer.service.JsonSchemaTransformerService;
import com.spldeolin.allison1875.docanalyzer.util.JsonSchemaTraverseUtils;

/**
 * @author Deolin 2020-09-12
 */
@Singleton
public class JsonSchemaTransformerServiceImpl implements JsonSchemaTransformerService {

    private static final ObjectMapper om = JsonUtils.createObjectMapper();

    @Override
    public void transformForEnum(JsonSchema rootJsonSchema) {
        JsonSchemaTraverseUtils.traverse(rootJsonSchema, (propertyName, jsonSchema, parentJsonSchema, depth) -> {
            if (jsonSchema.isValueTypeSchema()) {
                Set<String> enums = jsonSchema.asValueTypeSchema().getEnums();
                if (enums != null) {
                    List<AnalyzeEnumConstantRetval> ecats = Lists.newArrayList();
                    for (String anEnum : enums) {
                        try {
                            ecats.add(om.readValue(anEnum, AnalyzeEnumConstantRetval.class));
                        } catch (JsonProcessingException e) {
                            // 这种情况说明anEnum没有被AnalyzeEnumConstantHandle#analyzeEnumConstant转化为JSON
                        }
                    }

                    JsonPropertyDescriptionValueDto jpdv = JsonPropertyDescriptionValueDto.deserialize(
                            jsonSchema.getDescription());
                    if (jpdv != null) {
                        jpdv.setEcats(ecats);
                        jsonSchema.setDescription(jpdv.serialize());
                    }

                    if (parentJsonSchema.isArraySchema()) {
                        JsonPropertyDescriptionValueDto parentJpdv = JsonPropertyDescriptionValueDto.deserialize(
                                parentJsonSchema.getDescription());
                        if (parentJpdv == null) {
                            parentJpdv = new JsonPropertyDescriptionValueDto();
                        }
                        parentJpdv.setEcats(ecats);
                        parentJsonSchema.setDescription(parentJpdv.serialize());
                    }

                    enums.clear();
                }
            }
        });
    }

    @Override
    public void transformReferenceSchema(JsonSchema rootJsonSchema) {
        Map<String, String> pathsEachId = Maps.newHashMap();
        Map<JsonSchema, String> paths = Maps.newLinkedHashMap();
        if (rootJsonSchema.isObjectSchema()) {
            pathsEachId.put(rootJsonSchema.getId(), "根节点");
        }

        // 处理ReferenceSchema
        JsonSchemaTraverseUtils.traverse(rootJsonSchema, (propertyName, jsonSchema, parentJsonSchema, depth) -> {
            JsonPropertyDescriptionValueDto jpdv = JsonPropertyDescriptionValueDto.deserialize(
                    jsonSchema.getDescription());
            String path = paths.get(parentJsonSchema);
            if (path == null) {
                path = "";
            } else {
                if (parentJsonSchema.isObjectSchema()) {
                    path += ".";
                }
            }
            if (parentJsonSchema.isArraySchema()) {
                if (jsonSchema.isObjectSchema() || jsonSchema.isArraySchema()) {
                    propertyName = "";
                }
            }
            path = path + propertyName;
            if (jsonSchema.isArraySchema()) {
                path = path + "[]";
            }

            paths.put(jsonSchema, path);
            if (jsonSchema.getId() != null) {
                pathsEachId.put(jsonSchema.getId(), path);
            }

            if (jsonSchema instanceof ReferenceSchema) {
                String referencePath = pathsEachId.get(jsonSchema.get$ref());
                if (rootJsonSchema.isArraySchema()) {
                    referencePath = "根节点[]" + referencePath;
                }
                if (jpdv != null) {
                    jpdv.setReferencePath(referencePath);
                }
                if (parentJsonSchema.isArraySchema()) {
                    JsonPropertyDescriptionValueDto parentJpdv = JsonPropertyDescriptionValueDto.deserialize(
                            parentJsonSchema.getDescription());
                    if (parentJpdv == null) {
                        parentJpdv = new JsonPropertyDescriptionValueDto();
                    }
                    parentJpdv.setReferencePath(referencePath);
                    parentJsonSchema.setDescription(parentJpdv.serialize());
                }
            }

            if (jpdv != null) {
                jsonSchema.setDescription(jpdv.serialize());
            }
        });
    }

}
package com.spldeolin.allison1875.docanalyzer.processor;

import java.util.Map;
import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.fasterxml.jackson.module.jsonSchema.types.ReferenceSchema;
import com.google.common.collect.Maps;
import com.spldeolin.allison1875.base.util.JsonUtils;
import com.spldeolin.allison1875.docanalyzer.javabean.JsonPropertyDescriptionValueDto;
import com.spldeolin.allison1875.docanalyzer.util.JsonSchemaTraverseUtils;

/**
 * @author Deolin 2020-08-14
 */
public class ReferenceSchemaProc {

    private final JsonSchema rootJsonSchema;

    ReferenceSchemaProc(JsonSchema rootJsonSchema) {
        this.rootJsonSchema = rootJsonSchema;
    }

    public void process() {
        Map<String, String> pathsEachId = Maps.newHashMap();
        Map<JsonSchema, String> paths = Maps.newLinkedHashMap();
        if (rootJsonSchema.isObjectSchema()) {
            pathsEachId.put(rootJsonSchema.getId(), "根节点");
        }

        // 处理ReferenceSchema
        JsonSchemaTraverseUtils.traverse("根节点", rootJsonSchema, (propertyName, jsonSchema, parentJsonSchema) -> {
            JsonPropertyDescriptionValueDto jpdv = toJpdvSkipNull(jsonSchema.getDescription());
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
                if (this.rootJsonSchema.isArraySchema()) {
                    referencePath = "根节点[]" + referencePath;
                }
                if (jpdv != null) {
                    jpdv.setReferencePath(referencePath);
                }
                if (parentJsonSchema.isArraySchema()) {
                    JsonPropertyDescriptionValueDto parentJpdv = toJpdvSkipNull(parentJsonSchema.getDescription());
                    if (parentJpdv == null) {
                        parentJpdv = new JsonPropertyDescriptionValueDto();
                    }
                    parentJpdv.setReferencePath(referencePath);
                    parentJsonSchema.setDescription(JsonUtils.toJson(parentJpdv));
                }
            }

            if (jpdv != null) {
                jsonSchema.setDescription(JsonUtils.toJson(jpdv));
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
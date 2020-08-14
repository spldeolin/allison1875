package com.spldeolin.allison1875.docanalyzer.processor;

import java.util.Map;
import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.fasterxml.jackson.module.jsonSchema.types.ReferenceSchema;
import com.google.common.collect.Maps;
import com.spldeolin.allison1875.base.util.JsonUtils;
import com.spldeolin.allison1875.docanalyzer.dto.JsonPropertyDescriptionValueDto;
import com.spldeolin.allison1875.docanalyzer.util.JsonSchemaTraverseUtils;

/**
 * @author Deolin 2020-08-14
 */
public class ReferenceSchemaProc {

    private final JsonSchema jsonSchema;

    public ReferenceSchemaProc(JsonSchema jsonSchema) {
        this.jsonSchema = jsonSchema;
    }

    public void process() {
        Map<String, String> pathsEachId = Maps.newHashMap();
        Map<JsonSchema, String> paths = Maps.newLinkedHashMap();
        if (jsonSchema.isObjectSchema()) {
            pathsEachId.put(jsonSchema.getId(), "根节点");
        }

        // 处理ReferenceSchema
        JsonSchemaTraverseUtils.traverse("根节点", jsonSchema, (propertyName, jsonSchema, parentJsonSchema) -> {
            JsonPropertyDescriptionValueDto jpdv = JsonUtils
                    .toObjectSkipNull(jsonSchema.getDescription(), JsonPropertyDescriptionValueDto.class);
            String path = paths.get(parentJsonSchema);
            if (path == null) {
                path = "";
            } else {
                if (parentJsonSchema.isArraySchema()) {
                    path += "[]";
                }
                if (parentJsonSchema.isObjectSchema()) {
                    path += ".";
                }
            }
            path = path + propertyName;
            paths.put(jsonSchema, path);
            if (jsonSchema.getId() != null) {
                pathsEachId.put(jsonSchema.getId(), path);
            }

            if (jsonSchema instanceof ReferenceSchema) {
                String referencePath = pathsEachId.get(jsonSchema.get$ref());
                if (jpdv != null) {
                    jpdv.setReferencePath(referencePath);
                }
                if (parentJsonSchema.isArraySchema()) {
                    JsonPropertyDescriptionValueDto parentJpdv = JsonUtils
                            .toObjectSkipNull(parentJsonSchema.getDescription(), JsonPropertyDescriptionValueDto.class);
                    if (parentJpdv != null) {
                        parentJpdv.setReferencePath(referencePath);
                        parentJsonSchema.setDescription(JsonUtils.toJson(parentJpdv));
                    }
                }
            }

            if (jpdv != null) {
                jsonSchema.setDescription(JsonUtils.toJson(jpdv));
            }
        });
    }

}
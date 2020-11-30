package com.spldeolin.allison1875.docanalyzer.util;

import java.util.Map;
import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.fasterxml.jackson.module.jsonSchema.types.ArraySchema;
import com.fasterxml.jackson.module.jsonSchema.types.ArraySchema.Items;
import lombok.extern.log4j.Log4j2;

/**
 * @author Deolin 2020-08-02
 */
@Log4j2
public class JsonSchemaTraverseUtils {

    private JsonSchemaTraverseUtils() {
        throw new UnsupportedOperationException("Never instantiate me.");
    }

    public interface EveryJsonSchemaHandler {

        void handle(String propertyName, JsonSchema jsonSchema, JsonSchema parentJsonSchema);

    }

    public static void traverse(String propName, JsonSchema jsonSchema, EveryJsonSchemaHandler handler) {
        if (jsonSchema.isObjectSchema()) {
            Map<String, JsonSchema> properties = jsonSchema.asObjectSchema().getProperties();
            properties.forEach((childPropName, childJsonSchema) -> {
                handler.handle(childPropName, childJsonSchema, jsonSchema);
                traverse(childPropName, childJsonSchema, handler);
            });
        }
        if (jsonSchema.isArraySchema()) {
            traverseArray(propName, jsonSchema.asArraySchema(), handler, jsonSchema);
        }
    }

    private static void traverseArray(String propName, ArraySchema arraySchema, EveryJsonSchemaHandler handler,
            JsonSchema parentJsonSchema) {
        Items items = arraySchema.getItems();
        if (items == null) {
            return;
        }
        if (items.isArrayItems()) {
            // Java没有tuple语法，所以这个情况不可能存在
            for (JsonSchema tupleElementJsonSchema : items.asArrayItems().getJsonSchemas()) {
                handler.handle(propName, tupleElementJsonSchema, parentJsonSchema);
                traverse(propName, tupleElementJsonSchema, handler);
            }
        }
        if (items.isSingleItems()) {
            JsonSchema elementJsonSchema = items.asSingleItems().getSchema();
            handler.handle(propName, elementJsonSchema, parentJsonSchema);
            traverse(propName, elementJsonSchema, handler);
        }
    }

}
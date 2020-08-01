package com.spldeolin.allison1875.base.util;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.fasterxml.jackson.module.jsonSchema.JsonSchemaGenerator;
import com.fasterxml.jackson.module.jsonSchema.factories.SchemaFactoryWrapper;
import com.fasterxml.jackson.module.jsonSchema.factories.VisitorContext;
import com.spldeolin.allison1875.base.exception.DotAbsentInStringException;
import com.spldeolin.allison1875.base.util.exception.JsonSchemaException;
import lombok.extern.log4j.Log4j2;

/**
 * @author Deolin 2020-03-01
 */
@Log4j2
public class JsonSchemaUtils {

    private JsonSchemaUtils() {
        throw new UnsupportedOperationException("Never instantiate me.");
    }

    public static void main(String[] args) throws JsonSchemaException {
        JsonSchema jsonSchema = generateSchema("com.spldeolin.allison1875.base.collection.UserDto", DEFAULT_JSG);
        System.out.println(JsonUtils.toJson(jsonSchema));
        System.out.println(JsonUtils.toJson(JsonUtils.toJson(jsonSchema)));
    }

    public static final SchemaFactoryWrapper DEFAULT_SCHEMA_FACTORY_WRAPPER = new SchemaFactoryWrapper()
            .setVisitorContext(new VisitorContext() {
                @Override
                public String addSeenSchemaUri(JavaType aSeenSchema) {
                    return javaTypeToUrn(aSeenSchema);
                }
            });

    public static final JsonSchemaGenerator DEFAULT_JSG = new JsonSchemaGenerator(
            JsonUtils.initObjectMapper(new ObjectMapper()), DEFAULT_SCHEMA_FACTORY_WRAPPER);

    public static JsonSchema generateSchema(String describe, JsonSchemaGenerator jsg) throws JsonSchemaException {
        TypeFactory tf = TypeFactory.defaultInstance();

        try {
            return jsg.generateSchema(tf.constructFromCanonical(describe));
        } catch (Throwable e) {
            try {
                // 考虑describe可能是内部类，所以递归地将describe的最后一个.替换成$并重新尝试generateSchema
                return generateSchemaRecursively(describe, tf, jsg);
            } catch (DotAbsentInStringException dotAbsent) {
                // 即便递归到describe中没有.可替换了，依然generateSchema失败，递归尝试失败，说明describe本身就无法处理（无法处理的原因见方法Javadoc）
                log.warn("Cannot generate the json schema, qualifierForClassLoader={}, reason={}", describe,
                        e.getMessage());
                throw new JsonSchemaException(e);
            }
        }
    }

    private static JsonSchema generateSchemaRecursively(String innerClassMightDescribe, TypeFactory tf,
            JsonSchemaGenerator jsg) throws DotAbsentInStringException {
        try {
            return jsg.generateSchema(tf.constructFromCanonical(innerClassMightDescribe));
        } catch (Throwable e) {
            if (e instanceof StackOverflowError) {
                try {
                    return new JsonSchemaGenerator(JsonUtils.initObjectMapper(new ObjectMapper()))
                            .generateSchema(tf.constructFromCanonical(innerClassMightDescribe));
                } catch (Throwable e2) {
                    innerClassMightDescribe = tryReplaceLastDotToDollar(innerClassMightDescribe);
                    return generateSchemaRecursively(innerClassMightDescribe, tf, jsg);
                }
            }
            innerClassMightDescribe = tryReplaceLastDotToDollar(innerClassMightDescribe);
            return generateSchemaRecursively(innerClassMightDescribe, tf, jsg);
        }
    }

    private static String tryReplaceLastDotToDollar(String innerClassMightDescribe) throws DotAbsentInStringException {
        int lastDotIndex = innerClassMightDescribe.lastIndexOf('.');
        if (lastDotIndex == -1) {
            throw new DotAbsentInStringException();
        }
        return innerClassMightDescribe.substring(0, lastDotIndex) + '$' + innerClassMightDescribe
                .substring(lastDotIndex + 1);
    }


    public static String getId(JsonSchema jsonSchema) {
        return jsonSchema.getId().substring("urn:jsonschema:".length()).replace(':', '.');
    }

}

package com.spldeolin.allison1875.base.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.fasterxml.jackson.module.jsonSchema.JsonSchemaGenerator;
import com.spldeolin.allison1875.base.exception.DotAbsentInStringException;
import com.spldeolin.allison1875.base.util.exception.JsonSchemasException;
import lombok.extern.log4j.Log4j2;

/**
 * @author Deolin 2020-03-01
 */
@Log4j2
public class JsonSchemaUtils {

    private JsonSchemaUtils() {
        throw new UnsupportedOperationException("Never instantiate me.");
    }

    public static final JsonSchemaGenerator DEFAULT_JSG = new JsonSchemaGenerator(
            JsonUtils.initObjectMapper(new ObjectMapper()));

    public static JsonSchema generateSchema(String describe, ClassLoader cl, JsonSchemaGenerator jsg)
            throws JsonSchemasException {
        TypeFactory tf = buildTypeFactory(cl);

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
                throw new JsonSchemasException(e);
            }
        }
    }

    private static TypeFactory buildTypeFactory(ClassLoader cl) {
        return new TypeFactory(null) {
            @Override
            public ClassLoader getClassLoader() {
                return cl;
            }

            private static final long serialVersionUID = -3065446625827426521L;
        };
    }

    private static JsonSchema generateSchemaRecursively(String innerClassMightDescribe, TypeFactory tf,
            JsonSchemaGenerator jsg) throws DotAbsentInStringException {
        try {
            return jsg.generateSchema(tf.constructFromCanonical(innerClassMightDescribe));
        } catch (Throwable e) {
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

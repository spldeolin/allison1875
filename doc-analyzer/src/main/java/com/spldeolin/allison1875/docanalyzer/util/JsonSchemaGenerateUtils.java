package com.spldeolin.allison1875.docanalyzer.util;

import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.fasterxml.jackson.module.jsonSchema.JsonSchemaGenerator;
import com.fasterxml.jackson.module.jsonSchema.factories.SchemaFactoryWrapper;
import com.fasterxml.jackson.module.jsonSchema.factories.VisitorContext;
import com.spldeolin.allison1875.base.exception.DotAbsentInStringException;
import com.spldeolin.allison1875.base.util.JsonUtils;
import com.spldeolin.allison1875.base.util.exception.JsonSchemaException;
import lombok.extern.log4j.Log4j2;

/**
 * @author Deolin 2020-03-01
 */
@Log4j2
public class JsonSchemaGenerateUtils {

    private JsonSchemaGenerateUtils() {
        throw new UnsupportedOperationException("Never instantiate me.");
    }

    public static final JsonSchemaGenerator DEFAULT_JSG = new JsonSchemaGenerator(
            // OM
            JsonUtils.initObjectMapper(new ObjectMapper()),
            new SchemaFactoryWrapper().setVisitorContext(new VisitorContext() {
                /**
                 * 多个property是同一个Javabean时
                 * 确保这些property的类型都是ObjectSchema
                 * 而不是一个ObjectSchema外加其他的RefereceSchema
                 */
                @Override
                public String addSeenSchemaUri(JavaType aSeenSchema) {
                    return javaTypeToUrn(aSeenSchema);
                }

                @Override
                public String javaTypeToUrn(JavaType jt) {
                    return jt.toCanonical();
                }
            }));

    public static JsonSchema generateSchema(String describe, ObjectMapper om,
            AnnotationIntrospector annotationIntrospector, VisitorContext visitorContext) throws JsonSchemaException {
        if (om == null) {
            om = new ObjectMapper();
        }
        if (annotationIntrospector != null) {
            om.setAnnotationIntrospector(annotationIntrospector);
        }
        JsonSchemaGenerator jsg;
        if (visitorContext != null) {
            jsg = new JsonSchemaGenerator(om, new SchemaFactoryWrapper().setVisitorContext(visitorContext));
        } else {
            jsg = new JsonSchemaGenerator(om);
        }

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

}

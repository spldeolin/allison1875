package com.spldeolin.allison1875.base.util;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.fasterxml.jackson.module.jsonSchema.JsonSchemaGenerator;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.spldeolin.allison1875.base.util.exception.JsonSchemasException;
import lombok.extern.log4j.Log4j2;

/**
 * @author Deolin 2020-03-01
 */
@Log4j2
public class JsonSchemaUtils {

    private static final String idPrefix = "urn:jsonschema:";

    private static final JsonSchemaGenerator defaultJsonSchemaGenerator = new JsonSchemaGenerator(
            JsonUtils.initObjectMapper(new ObjectMapper()));

    private static final TypeFactory typeFactory = TypeFactory.defaultInstance();

    private JsonSchemaUtils() {
        throw new UnsupportedOperationException("Never instantiate me.");
    }

    @Deprecated
    public static JsonSchema generateSchema(ClassOrInterfaceDeclaration coid) throws JsonSchemasException {
        return generateSchema(qualifierForClassLoader(coid), defaultJsonSchemaGenerator);
    }


    @Deprecated
    public static JsonSchema generateSchema(ClassOrInterfaceDeclaration coid, JsonSchemaGenerator jsg)
            throws JsonSchemasException {
        return generateSchema(qualifierForClassLoader(coid), jsg);
    }


    @Deprecated
    public static JsonSchema generateSchema(String qualifierForClassLoader) throws JsonSchemasException {
        return generateSchema(qualifierForClassLoader, defaultJsonSchemaGenerator);
    }

    @Deprecated
    public static JsonSchema generateSchema(String qualifierForClassLoader, JsonSchemaGenerator jsg)
            throws JsonSchemasException {
        try {
            JavaType javaType = typeFactory.constructFromCanonical(qualifierForClassLoader);
            return jsg.generateSchema(javaType);
        } catch (Throwable e) {
            log.warn("Cannot generate the json schema, qualifierForClassLoader={}, reason={}", qualifierForClassLoader,
                    e.getMessage());
            throw new JsonSchemasException();
        }
    }

    public static JsonSchema generateSchema(String qualifierForClassLoader, ClassLoader classLoader,
            JsonSchemaGenerator jsg) throws JsonSchemasException {
        try {
            JavaType javaType = new TypeFactory(null) {
                @Override
                public ClassLoader getClassLoader() {
                    return classLoader;
                }

                private static final long serialVersionUID = -3065446625827426521L;
            }.constructFromCanonical(qualifierForClassLoader);
            return jsg.generateSchema(javaType);
        } catch (Throwable e) {
            // TODO qualifierForClassLoader可能是内部类，需要递归处理
            log.warn("Cannot generate the json schema, qualifierForClassLoader={}, reason={}", qualifierForClassLoader,
                    e.getMessage(), e);
            throw new JsonSchemasException();
        }
    }

    public static JsonSchema generateSchemaOrElseNull(String qualifierForClassLoader) {
        try {
            return generateSchema(qualifierForClassLoader);
        } catch (JsonSchemasException e) {
            return null;
        }
    }

    public static String getId(JsonSchema jsonSchema) {
        return jsonSchema.getId().substring(idPrefix.length()).replace(':', '.');
    }

    private static String qualifierForClassLoader(ClassOrInterfaceDeclaration coid) {
        StringBuilder qualifierForClassLoader = new StringBuilder(64);
        qualifierForClassLoader(qualifierForClassLoader, coid);
        return qualifierForClassLoader.toString();
    }

    private static void qualifierForClassLoader(StringBuilder qualifier, TypeDeclaration<?> node) {
        node.getParentNode().ifPresent(parent -> {
            if (parent instanceof TypeDeclaration) {
                qualifierForClassLoader(qualifier, (TypeDeclaration<?>) parent);
                qualifier.append("$");
                qualifier.append(node.getNameAsString());
            } else {
                node.getFullyQualifiedName().ifPresent(qualifier::append);
            }
        });
    }

}

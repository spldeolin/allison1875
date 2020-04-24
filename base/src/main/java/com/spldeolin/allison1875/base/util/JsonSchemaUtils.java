package com.spldeolin.allison1875.base.util;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.fasterxml.jackson.module.jsonSchema.JsonSchemaGenerator;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.spldeolin.allison1875.base.classloader.MavenProjectClassLoaderFactory;
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

    private static final TypeFactory typeFactory = new TypeFactory(null) {

        private static final long serialVersionUID = 2221941743132252200L;

        @Override
        public ClassLoader getClassLoader() {
            return MavenProjectClassLoaderFactory.getClassLoader();
        }
    };

    private JsonSchemaUtils() {
        throw new UnsupportedOperationException("Never instantiate me.");
    }

    public static JsonSchema generateSchema(ClassOrInterfaceDeclaration coid) throws JsonSchemasException {
        return generateSchema(qualifierForClassLoader(coid));
    }

    public static JsonSchema generateSchema(String qualifierForClassLoader) throws JsonSchemasException {
        try {
            JavaType javaType = typeFactory.constructFromCanonical(qualifierForClassLoader);
            return defaultJsonSchemaGenerator.generateSchema(javaType);
        } catch (Throwable e) {
            log.warn("Cannot generate the json schema, qualifierForClassLoader={}, reason={}", qualifierForClassLoader,
                    e.getMessage());
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
        node.getParentNode()
            .ifPresent(parent -> {
                if (parent instanceof TypeDeclaration) {
                    qualifierForClassLoader(qualifier, (TypeDeclaration<?>) parent);
                    qualifier.append("$");
                    qualifier.append(node.getNameAsString());
                } else {
                    node.getFullyQualifiedName()
                        .ifPresent(qualifier::append);
                }
            });
    }

}

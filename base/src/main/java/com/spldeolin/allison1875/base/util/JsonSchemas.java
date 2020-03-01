package com.spldeolin.allison1875.base.util;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.fasterxml.jackson.module.jsonSchema.JsonSchemaGenerator;
import com.spldeolin.allison1875.base.classloader.WarOrFatJarClassLoaderFactory;

/**
 * @author Deolin 2020-03-01
 */
public class JsonSchemas {

    private static final JsonSchemaGenerator defaultJsonSchemaGenerator;

    static {
        defaultJsonSchemaGenerator = new JsonSchemaGenerator(Jsons.newDefaultObjectMapper());
    }

    public static JsonSchema generateSchema(String qualifierForClassLoader) throws JsonMappingException {
        JavaType javaType;
        javaType = new TypeFactory(null) {

            private static final long serialVersionUID = 2221941743132252200L;

            @Override
            public ClassLoader getClassLoader() {
                return WarOrFatJarClassLoaderFactory.getClassLoader();
            }
        }.constructFromCanonical(qualifierForClassLoader);
        return defaultJsonSchemaGenerator.generateSchema(javaType);
    }

}

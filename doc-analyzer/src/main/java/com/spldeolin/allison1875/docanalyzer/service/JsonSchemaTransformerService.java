package com.spldeolin.allison1875.docanalyzer.service;

import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.google.inject.ImplementedBy;
import com.spldeolin.allison1875.docanalyzer.service.impl.JsonSchemaTransformerServiceImpl;

/**
 * @author Deolin 2023-12-23
 */
@ImplementedBy(JsonSchemaTransformerServiceImpl.class)
public interface JsonSchemaTransformerService {

    void transformReferenceSchema(JsonSchema rootJsonSchema);

}
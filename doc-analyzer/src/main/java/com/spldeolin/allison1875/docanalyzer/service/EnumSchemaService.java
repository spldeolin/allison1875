package com.spldeolin.allison1875.docanalyzer.service;

import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.google.inject.ImplementedBy;
import com.spldeolin.allison1875.docanalyzer.service.impl.EnumSchemaServiceImpl;

/**
 * @author Deolin 2023-12-23
 */
@ImplementedBy(EnumSchemaServiceImpl.class)
public interface EnumSchemaService {

    void resolve(JsonSchema rootJsonSchema);

}
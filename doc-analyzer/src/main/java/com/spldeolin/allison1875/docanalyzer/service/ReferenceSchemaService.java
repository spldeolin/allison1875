package com.spldeolin.allison1875.docanalyzer.service;

import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.google.inject.ImplementedBy;
import com.spldeolin.allison1875.docanalyzer.service.impl.ReferenceSchemaServiceImpl;

/**
 * @author Deolin 2023-12-23
 */
@ImplementedBy(ReferenceSchemaServiceImpl.class)
public interface ReferenceSchemaService {

    void process(JsonSchema rootJsonSchema);

}
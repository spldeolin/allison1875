package com.spldeolin.allison1875.da.core.domain;

import java.util.Collection;
import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.spldeolin.allison1875.da.core.enums.BodyType;
import com.spldeolin.allison1875.da.core.enums.MethodType;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author Deolin 2019-12-02
 */
@Data
@Accessors(fluent = true)
public class ApiDomain {

    private Collection<MethodType> method;

    private Collection<String> uri;

    private String description;

    private Collection<UriFieldDomain> pathVariableFields;

    private Collection<UriFieldDomain> requestParamFields;

    private BodyType requestBodyType;

    private Collection<BodyFieldDomain> requestBodyFields;

    private Collection<BodyFieldDomain> requestBodyFieldsFlatly;

    private JsonSchema requestBodyChaosJsonSchema;

    private BodyType responseBodyType;

    private Collection<BodyFieldDomain> responseBodyFields;

    private Collection<BodyFieldDomain> responseBodyFieldsFlatly;

    private JsonSchema responseBodyChaosJsonSchema;

    private String codeSourceLocation;

}
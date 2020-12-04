package com.spldeolin.allison1875.docanalyzer.dto;

import java.util.Collection;
import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import lombok.Data;

/**
 * @author Deolin 2020-06-01
 */
@Data
public class EndpointDto {

    private String cat;

    private String handlerSimpleName;

    private Collection<String> descriptionLines;

    private String url;

    private String httpMethod;

    private Boolean isDeprecated;

    private JsonSchema requestBodyJsonSchema;

    private JsonSchema responseBodyJsonSchema;

    private String author;

    private String sourceCode;

    public EndpointDto copy() {
        EndpointDto result = new EndpointDto();
        result.setCat(cat);
        result.setHandlerSimpleName(handlerSimpleName);
        result.setDescriptionLines(descriptionLines);
        result.setUrl(url);
        result.setHttpMethod(httpMethod);
        result.setIsDeprecated(isDeprecated);
        result.setRequestBodyJsonSchema(requestBodyJsonSchema);
        result.setResponseBodyJsonSchema(responseBodyJsonSchema);
        result.setAuthor(author);
        result.setSourceCode(sourceCode);
        return result;
    }

}
package com.spldeolin.allison1875.docanalyzer.javabean;

import java.util.List;
import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import lombok.Data;

/**
 * @author Deolin 2020-06-01
 */
@Data
public class EndpointDto {

    private String cat;

    private String handlerSimpleName;

    private List<String> descriptionLines;

    private String url;

    private String httpMethod;

    private Boolean isDeprecated;

    private String requestBodyDescribe;

    private JsonSchema requestBodyJsonSchema;

    private String responseBodyDescribe;

    private JsonSchema responseBodyJsonSchema;

    private String author;

    private String sourceCode;

    private Object more;

    public EndpointDto copy() {
        EndpointDto result = new EndpointDto();
        result.setCat(cat);
        result.setHandlerSimpleName(handlerSimpleName);
        result.setDescriptionLines(descriptionLines);
        result.setUrl(url);
        result.setHttpMethod(httpMethod);
        result.setIsDeprecated(isDeprecated);
        result.setRequestBodyDescribe(requestBodyDescribe);
        result.setRequestBodyJsonSchema(requestBodyJsonSchema);
        result.setResponseBodyDescribe(responseBodyDescribe);
        result.setResponseBodyJsonSchema(responseBodyJsonSchema);
        result.setAuthor(author);
        result.setSourceCode(sourceCode);
        result.setMore(more);
        return result;
    }

}
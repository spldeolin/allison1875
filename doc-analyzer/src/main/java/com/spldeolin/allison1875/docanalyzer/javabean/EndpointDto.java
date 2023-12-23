package com.spldeolin.allison1875.docanalyzer.javabean;

import java.util.List;
import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

/**
 * @author Deolin 2020-06-01
 */
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EndpointDto {

    String cat;

    String handlerSimpleName;

    List<String> descriptionLines;

    String url;

    String httpMethod;

    Boolean isDeprecated;

    String requestBodyDescribe;

    JsonSchema requestBodyJsonSchema;

    String responseBodyDescribe;

    JsonSchema responseBodyJsonSchema;

    String author;

    String sourceCode;

    Object more;

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
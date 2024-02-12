package com.spldeolin.allison1875.docanalyzer.javabean;

import java.util.List;
import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

/**
 * @author Deolin 2020-06-01
 */
@Data
@Accessors(chain = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EndpointDto {

    String cat;

    String handlerSimpleName;

    List<String> descriptionLines;

    Boolean isDeprecated;

    String author;

    String sourceCode;

    Object more;

    String url;

    String httpMethod;

    String requestBodyDescribe;

    JsonSchema requestBodyJsonSchema;

    String responseBodyDescribe;

    JsonSchema responseBodyJsonSchema;

    public EndpointDto copy() {
        EndpointDto result = new EndpointDto();
        result.setCat(cat);
        result.setHandlerSimpleName(handlerSimpleName);
        result.setDescriptionLines(descriptionLines);
        result.setIsDeprecated(isDeprecated);
        result.setAuthor(author);
        result.setSourceCode(sourceCode);
        result.setMore(more);
        result.setUrl(url);
        result.setHttpMethod(httpMethod);
        result.setRequestBodyDescribe(requestBodyDescribe);
        result.setRequestBodyJsonSchema(requestBodyJsonSchema);
        result.setResponseBodyDescribe(responseBodyDescribe);
        result.setResponseBodyJsonSchema(responseBodyJsonSchema);
        return result;
    }

}
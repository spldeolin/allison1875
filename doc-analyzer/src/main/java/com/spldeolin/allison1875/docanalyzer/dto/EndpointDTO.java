package com.spldeolin.allison1875.docanalyzer.dto;

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
public class EndpointDTO {

    String cat;

    List<String> descriptionLines;

    List<String> reqBodyParamDescriptionLines;

    List<String> returnDescriptionLines;

    Boolean isDeprecated;

    String author;

    String sourceCode;

    List<String> urls;

    String httpMethod;

    List<QueryParamDTO> queryParams;

    List<PathParamDTO> pathParams;

    String requestBodyDescribe;

    JsonSchema requestBodyJsonSchema;

    String responseBodyDescribe;

    JsonSchema responseBodyJsonSchema;

    public EndpointDTO copy() {
        EndpointDTO result = new EndpointDTO();
        result.setCat(cat);
        result.setDescriptionLines(descriptionLines);
        result.setReqBodyParamDescriptionLines(reqBodyParamDescriptionLines);
        result.setReturnDescriptionLines(returnDescriptionLines);
        result.setIsDeprecated(isDeprecated);
        result.setAuthor(author);
        result.setSourceCode(sourceCode);
        result.setUrls(urls);
        result.setHttpMethod(httpMethod);
        result.setQueryParams(queryParams);
        result.setPathParams(pathParams);
        result.setRequestBodyDescribe(requestBodyDescribe);
        result.setRequestBodyJsonSchema(requestBodyJsonSchema);
        result.setResponseBodyDescribe(responseBodyDescribe);
        result.setResponseBodyJsonSchema(responseBodyJsonSchema);
        return result;
    }

}
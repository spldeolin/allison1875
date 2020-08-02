package com.spldeolin.allison1875.docanalyzer.builder;

import java.util.Collection;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.RequestMethod;
import com.spldeolin.allison1875.docanalyzer.dto.EndpointDto;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author Deolin 2020-06-01
 */
@Data
@Accessors(fluent = true)
public class EndpointDtoBuilder {

    private String cat;

    private String handlerSimpleName;

    private Collection<String> descriptionLines;

    private boolean isDeprecated;

    private Collection<String> combinedUrls;

    private Collection<RequestMethod> combinedVerbs;

    private RequestBodyInfoBuilder requestBodyInfo;

    private ResponseBodyInfoBuilder responseBodyInfo;

    private String author;

    private String sourceCode;

    public EndpointDto build() {
        EndpointDto result = new EndpointDto();
        result.setCat(cat);
        result.setHandlerSimpleName(handlerSimpleName);
        result.setDescriptionLines(descriptionLines);
        result.setUrls(combinedUrls);
        result.setHttpMethods(
                combinedVerbs.stream().map(one -> StringUtils.lowerCase(one.name())).collect(Collectors.toList()));
        result.setIsDeprecated(isDeprecated);
        result.setRequestBodySituation(requestBodyInfo.requestBodySituation());
        result.setRequestBodyJsonSchema(requestBodyInfo.requestBodyJsonSchema());
        result.setRequestBodyProperties(requestBodyInfo.flatRequestProperties());
        result.setResponseBodySituation(responseBodyInfo.responseBodySituation());
        result.setResponseBodyJsonSchema(responseBodyInfo.responseBodyJsonSchema());
        result.setResponseBodyProperties(responseBodyInfo.flatResponseProperties());
        result.setAuthor(author);
        result.setSourceCode(sourceCode);
        return result;
    }

}

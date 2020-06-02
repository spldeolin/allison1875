package com.spldeolin.allison1875.da.approved.builder;

import java.util.Collection;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.RequestMethod;
import com.spldeolin.allison1875.da.approved.dto.EndpointDto;
import com.spldeolin.allison1875.da.approved.dto.PropertyDto;
import com.spldeolin.allison1875.da.approved.enums.BodySituation;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author Deolin 2020-06-01
 */
@Data
@Accessors(fluent = true)
public class EndpointDtoBuilder {

    private String description;

    private String version;

    private boolean isDeprecated;

    private Collection<String> combinedUrls;

    private Collection<RequestMethod> combinedVerbs;

    private BodySituation requestBodySituation;

    private String requestBodyJsonSchema;

    private Collection<PropertyDto> flatRequestProperties;

    private boolean isResponseBodyNone;

    private boolean isResponseBodyChaos;

    private BodySituation responseBodySituation;

    private String responseBodyJsonSchema;

    private Collection<PropertyDto> flatResponseProperties;

    private String author;

    private String sourceCode;

    public EndpointDto build() {
        EndpointDto result = new EndpointDto();
        result.setDescription(description);
        result.setUrls(combinedUrls);
        result.setHttpMethods(
                combinedVerbs.stream().map(one -> StringUtils.lowerCase(one.name())).collect(Collectors.toList()));
        result.setEndpointVersion(version);
        result.setIsDeprecated(isDeprecated);
        result.setRequestBodySituation(requestBodySituation);
        result.setRequestBodyJsonSchema(requestBodyJsonSchema);
        result.setRequestBodyProperties(flatRequestProperties);

        result.setResponseBodySituation(responseBodySituation);
        result.setResponseBodyJsonSchema(responseBodyJsonSchema);
        result.setResponseBodyProperties(flatResponseProperties);
        result.setAuthor(author);
        result.setSourceCode(sourceCode);
        return result;
    }

    public static void main(String[] args) {
        System.out.println(StringUtils.lowerCase(RequestMethod.GET.name()));
    }

}

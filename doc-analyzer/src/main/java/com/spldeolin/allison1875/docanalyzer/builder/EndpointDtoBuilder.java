package com.spldeolin.allison1875.docanalyzer.builder;

import java.util.Collection;
import org.springframework.web.bind.annotation.RequestMethod;
import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.spldeolin.allison1875.base.util.StringUtils;
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

    private JsonSchema requestBodyJsonSchema;

    private JsonSchema responseBodyJsonSchema;

    private String author;

    private String sourceCode;

    public Collection<EndpointDto> build() {
        Collection<EndpointDto> result = Lists.newArrayList();
        for (String combinedUrl : combinedUrls) {
            EndpointDto dto = new EndpointDto();
            dto.setCat(cat);
            dto.setHandlerSimpleName(handlerSimpleName);
            dto.setDescriptionLines(descriptionLines);
            dto.setUrl(combinedUrl);
            if (combinedVerbs.contains(RequestMethod.POST)) {
                dto.setHttpMethod("post");
            } else {
                RequestMethod first = Iterables.getFirst(combinedVerbs, null);
                if (first != null) {
                    dto.setHttpMethod(StringUtils.lowerCase(first.toString()));
                } else {
                    dto.setHttpMethod("");
                }
            }
            dto.setIsDeprecated(isDeprecated);
            dto.setRequestBodyJsonSchema(requestBodyJsonSchema);
            dto.setResponseBodyJsonSchema(responseBodyJsonSchema);
            dto.setAuthor(author);
            dto.setSourceCode(sourceCode);
            result.add(dto);
        }
        return result;
    }

}

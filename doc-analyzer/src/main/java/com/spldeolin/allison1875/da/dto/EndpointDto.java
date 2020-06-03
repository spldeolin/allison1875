package com.spldeolin.allison1875.da.dto;

import java.util.Collection;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.spldeolin.allison1875.base.util.JsonUtils;
import com.spldeolin.allison1875.da.enums.BodySituation;
import lombok.Data;

/**
 * @author Deolin 2020-06-01
 */
@Data
@JsonInclude(Include.NON_NULL)
public class EndpointDto {

    private String description;

    private Collection<String> urls;

    private Collection<String> httpMethods;

    private String endpointVersion;

    private Boolean isDeprecated;

    private BodySituation requestBodySituation;

    /**
     * requestBodySituation为CHAOS时，不为null，内容是raw json
     */
    private String requestBodyJsonSchema;

    /**
     * requestBodySituation为NEITHER时，不为null
     */
    private Collection<PropertyDto> requestBodyProperties;

    private BodySituation responseBodySituation;

    /**
     * responseBodySituation为CHAOS时，不为null，内容是raw json
     */
    private String responseBodyJsonSchema;

    /**
     * responseBodySituation为NEITHER时，不为null
     */
    private Collection<PropertyDto> responseBodyProperties;

    private String author;

    private String sourceCode;

    @Override
    public String toString() {
        return JsonUtils.beautify(this);
    }

}
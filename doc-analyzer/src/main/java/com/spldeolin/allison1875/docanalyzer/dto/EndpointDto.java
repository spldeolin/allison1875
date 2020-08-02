package com.spldeolin.allison1875.docanalyzer.dto;

import java.util.Collection;
import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.spldeolin.allison1875.base.util.JsonUtils;
import com.spldeolin.allison1875.docanalyzer.enums.BodySituationEnum;
import lombok.Data;

/**
 * @author Deolin 2020-06-01
 */
@Data
public class EndpointDto {

    /**
     * 所在组的名称，如果是多层组的话，名称间使用半角句号分割
     */
    private String cat;

    private String description;

    private Collection<String> urls;

    private Collection<String> httpMethods;

    private String endpointVersion;

    private Boolean isDeprecated;

    private BodySituationEnum requestBodySituation;

    private JsonSchema requestBodyJsonSchema;

    /**
     * requestBodySituation为NEITHER时，不为null
     */
    private Collection<PropertyDto> requestBodyProperties;

    private BodySituationEnum responseBodySituation;

    private JsonSchema responseBodyJsonSchema;

    /**
     * responseBodySituation为NEITHER时，不为null
     */
    private Collection<PropertyDto> responseBodyProperties;

    private String author;

    private String sourceCode;

    @Override
    public String toString() {
        return JsonUtils.toJsonPrettily(this);
    }

}
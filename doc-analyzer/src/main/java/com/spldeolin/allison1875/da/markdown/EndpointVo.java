package com.spldeolin.allison1875.da.markdown;

import java.util.Collection;
import lombok.Data;

/**
 * @author Deolin 2020-02-17
 */
@Data
public class EndpointVo {

    private String uri;

    private String httpMethod;

    private String description;

    private Integer requestBodySituation;

    private String requestBodyJsonSchema;

    private Collection<RequestBodyPropertyVo> requestBodyProperties;

    private Boolean isAnyRequestBodyPropertyEnum;

    private Boolean anyValidatorsExist;

    private Integer responseBodySituation;

    private String responseBodyJsonSchema;

    private Collection<ResponseBodyPropertyVo> responseBodyProperties;

    private Boolean isAnyResponseBodyPropertyEnum;

    private String author;

    private String sourceCode;

}
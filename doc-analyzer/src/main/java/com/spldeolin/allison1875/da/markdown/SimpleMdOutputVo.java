package com.spldeolin.allison1875.da.markdown;

import java.util.Collection;
import lombok.Data;

/**
 * @author Deolin 2020-02-17
 */
@Data
public class SimpleMdOutputVo {

    private String uri;

    private String description;

    private Integer requestBodySituation;

    private Collection<RequestBodyFieldVo> requestBodyFields;

    private Integer responseBodySituation;

    private Collection<ResponseBodyFieldVo> responseBodyFields;

    private String author;

    private String sourceCode;

}
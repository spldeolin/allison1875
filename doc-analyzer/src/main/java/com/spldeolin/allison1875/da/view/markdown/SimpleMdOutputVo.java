package com.spldeolin.allison1875.da.view.markdown;

import java.util.Collection;
import lombok.Data;

/**
 * @author Deolin 2020-02-17
 */
@Data
public class SimpleMdOutputVo {

    private String uri;

    private String description;

    private Boolean isRequestBodyNone;

    private Boolean isRequestBodyChaos;

    private Boolean anyValidatorsExist;

    private Collection<RequestBodyFieldVo> requestBodyFields;

    private Boolean isResponseBodyNone;

    private Boolean isResponseBodyChaos;

    private Collection<ResponseBodyFieldVo> responseBodyFields;

    private String author;

    private String location;

}
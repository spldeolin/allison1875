package com.spldeolin.allison1875.da.view.markdown;

import java.util.Collection;
import lombok.Data;

/**
 * @author Deolin 2020-02-17
 */
@Data
public class SimpleMdOutputFtl {

    private String uri;

    private String description;

    private Boolean isRequestBodyNone;

    private Boolean isRequestBodyChaos;

    private Collection<RequestBodyFieldFtl> requestBodyFields;

    private Boolean isResponseBodyNode;

    private Boolean isResponseBodyChaos;

    private Collection<ResponseBodyFieldFtl> responseBodyFieldVms;

}
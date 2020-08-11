package com.spldeolin.allison1875.docanalyzer.dto;

import java.util.Collection;
import lombok.Data;

/**
 * @author Deolin 2020-06-01
 */
@Data
public class DocDto {

    private String urlPrefix;

    private String docVersion;

    private Collection<EndpointDto> endpoints;

}

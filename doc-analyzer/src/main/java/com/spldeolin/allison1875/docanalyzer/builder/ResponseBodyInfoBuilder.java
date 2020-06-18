package com.spldeolin.allison1875.docanalyzer.builder;

import java.util.Collection;
import com.spldeolin.allison1875.docanalyzer.dto.PropertyDto;
import com.spldeolin.allison1875.docanalyzer.enums.BodySituationEnum;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author Deolin 2020-06-10
 */
@Data
@Accessors(fluent = true)
public class ResponseBodyInfoBuilder {

    private BodySituationEnum responseBodySituation;

    private String responseBodyJsonSchema;

    private Collection<PropertyDto> flatResponseProperties;

}
package com.spldeolin.allison1875.da.builder;

import java.util.Collection;
import com.spldeolin.allison1875.da.dto.PropertyDto;
import com.spldeolin.allison1875.da.enums.BodySituationEnum;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author Deolin 2020-06-10
 */
@Data
@Accessors(fluent = true)
public class RequestBodyInfoResult {

    private BodySituationEnum requestBodySituation;

    private String requestBodyJsonSchema;

    private Collection<PropertyDto> flatRequestProperties;

}

package com.spldeolin.allison1875.da.core.processor.result;

import java.util.Set;
import com.spldeolin.allison1875.da.core.enums.MethodTypeEnum;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author Deolin 2020-01-06
 */
@Data
@Accessors(fluent = true)
public class RequestMappingProcessResult {

    private Set<MethodTypeEnum> methodTypes;

    private Set<String> uris;

}

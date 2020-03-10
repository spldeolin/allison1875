package com.spldeolin.allison1875.da.core.definition;

import com.spldeolin.allison1875.da.core.enums.ValidatorTypeEnum;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author Deolin 2019-12-23
 */
@Data
@Accessors(chain = true)
public class ValidatorDefinition {

    private ValidatorTypeEnum validatorType;

    private String note;

}

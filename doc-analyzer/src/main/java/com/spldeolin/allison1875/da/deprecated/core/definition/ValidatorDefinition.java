package com.spldeolin.allison1875.da.deprecated.core.definition;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.spldeolin.allison1875.da.deprecated.core.enums.ValidatorTypeEnum;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author Deolin 2019-12-23
 */
@Data
@Accessors(chain = true)
@JsonInclude(Include.NON_NULL)
public class ValidatorDefinition {

    private ValidatorTypeEnum validatorType;

    private String note;

}

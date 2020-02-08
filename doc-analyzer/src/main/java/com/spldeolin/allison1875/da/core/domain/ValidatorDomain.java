package com.spldeolin.allison1875.da.core.domain;

import com.spldeolin.allison1875.da.core.enums.ValidatorType;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author Deolin 2019-12-23
 */
@Data
@Accessors(fluent = true)
public class ValidatorDomain {

    private ValidatorType validatorType;

    private String note;

}

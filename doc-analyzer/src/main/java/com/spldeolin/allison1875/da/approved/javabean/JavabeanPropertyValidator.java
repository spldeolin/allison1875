package com.spldeolin.allison1875.da.approved.javabean;


import com.spldeolin.allison1875.da.approved.enums.ValidatorTypeEnum;

import lombok.Data;

/**
 * @author Deolin 2020-04-25
 */
@Data
public class JavabeanPropertyValidator {

    private ValidatorTypeEnum validatorType;

    private String note;

}

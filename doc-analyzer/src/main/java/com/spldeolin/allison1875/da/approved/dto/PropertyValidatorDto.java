package com.spldeolin.allison1875.da.approved.dto;


import com.spldeolin.allison1875.da.approved.enums.ValidatorTypeEnum;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author Deolin 2020-04-25
 */
@Data
@Accessors(chain = true)
public class PropertyValidatorDto {

    /**
     * @see ValidatorTypeEnum
     */
    private String validatorType;

    private String note;

}

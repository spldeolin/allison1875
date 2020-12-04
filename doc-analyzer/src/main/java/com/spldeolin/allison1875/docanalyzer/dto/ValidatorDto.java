package com.spldeolin.allison1875.docanalyzer.dto;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.spldeolin.allison1875.docanalyzer.enums.ValidatorTypeEnum;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author Deolin 2020-04-25
 */
@JsonInclude(Include.NON_NULL)
@Data
@Accessors(chain = true)
public class ValidatorDto {

    /**
     * @see ValidatorTypeEnum
     */
    private String validatorType;

    private String note;

}

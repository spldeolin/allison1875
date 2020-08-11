package com.spldeolin.allison1875.docanalyzer.dto;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.spldeolin.allison1875.docanalyzer.enums.ValidTypeEnum;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author Deolin 2020-04-25
 */
@Data
@Accessors(chain = true)
@JsonInclude(Include.NON_NULL)
public class ValidatorDto {

    /**
     * @see ValidTypeEnum
     */
    private String validatorType;

    private String note;

}

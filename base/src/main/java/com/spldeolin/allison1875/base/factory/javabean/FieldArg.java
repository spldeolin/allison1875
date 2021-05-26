package com.spldeolin.allison1875.base.factory.javabean;

import javax.validation.constraints.NotBlank;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author Deolin 2021-05-26
 */
@Data
@Accessors(chain = true)
public class FieldArg {

    private String typeQualifier;

    private String description;

    @NotBlank
    private String typeName;

    @NotBlank
    private String fieldName;

}
package com.spldeolin.allison1875.da.deprecated.core.definition;

import java.util.Collection;
import com.spldeolin.allison1875.da.deprecated.core.enums.FieldTypeEnum;
import com.spldeolin.allison1875.da.deprecated.core.enums.NumberFormatTypeEnum;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author Deolin 2020-01-17
 */
@Data
@Accessors(fluent = true)
public class UriFieldDefinition {

    private String fieldName;

    /**
     * @see FieldTypeEnum
     * @see ApiDefinition#pathVariableFields() string, number, boolean
     * @see ApiDefinition#requestParamFields() string, number, boolean
     */
    private FieldTypeEnum jsonType;

    private String stringFormat;

    private NumberFormatTypeEnum numberFormat;

    private Boolean required;

    private Collection<ValidatorDefinition> validators;

}

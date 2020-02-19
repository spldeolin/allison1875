package com.spldeolin.allison1875.da.core.domain;

import java.util.Collection;
import com.spldeolin.allison1875.da.core.enums.FieldTypeEnum;
import com.spldeolin.allison1875.da.core.enums.NumberFormatTypeEnum;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author Deolin 2020-01-17
 */
@Data
@Accessors(fluent = true)
public class UriFieldDomain {

    private String fieldName;

    /**
     * @see FieldTypeEnum
     * @see ApiDomain#pathVariableFields() string, number, boolean
     * @see ApiDomain#requestParamFields() string, number, boolean
     */
    private FieldTypeEnum jsonType;

    private String stringFormat;

    private NumberFormatTypeEnum numberFormat;

    private Boolean required;

    private Collection<ValidatorDomain> validators;

}

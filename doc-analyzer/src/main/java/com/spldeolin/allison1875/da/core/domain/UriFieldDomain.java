package com.spldeolin.allison1875.da.core.domain;

import java.util.Collection;
import com.spldeolin.allison1875.da.core.enums.FieldType;
import com.spldeolin.allison1875.da.core.enums.NumberFormatType;
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
     * @see FieldType
     * @see ApiDomain#pathVariableFields() string, number, boolean
     * @see ApiDomain#requestParamFields() string, number, boolean
     */
    private FieldType jsonType;

    private String stringFormat;

    private NumberFormatType numberFormat;

    private Boolean required;

    private Collection<ValidatorDomain> validators;

}

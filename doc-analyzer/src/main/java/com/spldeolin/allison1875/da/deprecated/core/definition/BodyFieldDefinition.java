package com.spldeolin.allison1875.da.deprecated.core.definition;

import java.util.Collection;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.spldeolin.allison1875.da.deprecated.core.enums.FieldTypeEnum;
import com.spldeolin.allison1875.da.deprecated.core.enums.NumberFormatTypeEnum;
import lombok.Data;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * @author Deolin 2019-12-02
 */
@Data
@Accessors(chain = true)
@ToString(exclude = {"parentField"}) // StackOverflowError
@JsonInclude(Include.NON_NULL)
public class BodyFieldDefinition {

    /**
     * e.g.: city
     */
    private BodyFieldDefinition parentField;

    /**
     * e.g.: userDetails[0].address.city
     */
    private String linkName;

    private String fieldName;

    private FieldTypeEnum jsonType;

    private String stringFormat;

    private NumberFormatTypeEnum numberFormat;

    /**
     * notNull absent & notEmpty absent & notBlank absent = TRUE
     */
    private Boolean nullable;

    private Collection<ValidatorDefinition> validators;

    private String description;

    private Collection<BodyFieldDefinition> childFields;

}
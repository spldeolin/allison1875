package com.spldeolin.allison1875.querytransformer.dto;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

/**
 * @author Deolin 2026-06-26
 */
@Data
@Accessors(chain = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ExpandedFieldDTO {

    String typeQualifier;

    String fieldName;

    String description;

    String sourceExpression;

}

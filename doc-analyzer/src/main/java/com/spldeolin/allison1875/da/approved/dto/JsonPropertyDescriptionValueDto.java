package com.spldeolin.allison1875.da.approved.dto;

import java.util.Collection;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Deolin 2020-04-27
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class JsonPropertyDescriptionValueDto {

    String comment;

    Boolean nullable;

    Collection<PropertyValidatorDto> validators;

    String rawType;

}
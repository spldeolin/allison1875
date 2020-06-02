package com.spldeolin.allison1875.da.approved.dto;

import java.util.Collection;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Deolin 2020-04-27
 */
@Data
@JsonInclude(Include.NON_NULL)
public class JsonPropertyDescriptionValueDto {

    private String comment;

    private Boolean nullable;

    private Collection<PropertyValidatorDto> validators;

    private String rawType;

    private String jsonFormatPattern;

}
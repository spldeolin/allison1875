package com.spldeolin.allison1875.da.dto;

import java.util.Collection;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.Data;

/**
 * @author Deolin 2020-04-27
 */
@Data
@JsonInclude(Include.NON_NULL)
public class JsonPropertyDescriptionValueDto {

    private String comment;

    private Boolean required;

    private Collection<ValidatorDto> validators;

    private String rawType;

    private String jsonFormatPattern;

}
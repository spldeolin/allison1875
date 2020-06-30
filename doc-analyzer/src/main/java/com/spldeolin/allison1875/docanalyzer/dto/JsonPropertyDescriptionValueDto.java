package com.spldeolin.allison1875.docanalyzer.dto;

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

    private String description;

    private Collection<ValidatorDto> validators;

    private String rawType;

    private String jsonFormatPattern;

}
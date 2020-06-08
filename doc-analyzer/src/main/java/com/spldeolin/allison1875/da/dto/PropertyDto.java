package com.spldeolin.allison1875.da.dto;

import java.util.Collection;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.spldeolin.allison1875.da.enums.JsonTypeEnum;
import lombok.Data;

/**
 * @author Deolin 2020-06-03
 */
@Data
@JsonInclude(Include.NON_NULL)
public class PropertyDto {

    private Long id;

    private String path;

    private String name;

    private String description;

    private JsonTypeEnum jsonType;

    private String jsonFormat;

    private Boolean required;

    private Collection<PropertyValidatorDto> validators;

    @JsonInclude
    private Long parentId;

    private Collection<Long> childIds;

}
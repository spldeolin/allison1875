package com.spldeolin.allison1875.da.dto;

import java.util.Collection;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.spldeolin.allison1875.base.util.JsonUtils;
import com.spldeolin.allison1875.da.enums.JsonTypeEnum;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author Deolin 2020-04-25
 */
@Data
@JsonInclude(Include.NON_NULL)
@JsonIgnoreProperties("parent")
@Accessors(chain = true)
public class PropertyTreeNodeDto {

    private String uuid;

    private String name;

    private String description;

    private JsonTypeEnum jsonType;

    private String jsonFormat;

    private Boolean required;

    private Collection<PropertyValidatorDto> validators;

    private PropertyTreeNodeDto parent;

    private Collection<PropertyTreeNodeDto> children;

    @Override
    public String toString() {
        return JsonUtils.beautify(this);
    }

}

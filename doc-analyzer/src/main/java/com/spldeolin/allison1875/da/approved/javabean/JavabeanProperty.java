package com.spldeolin.allison1875.da.approved.javabean;

import java.util.Collection;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.spldeolin.allison1875.base.util.JsonUtils;
import com.spldeolin.allison1875.da.approved.enums.JsonTypeEnum;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author Deolin 2020-04-25
 */
@Data
@JsonInclude(Include.NON_NULL)
@JsonIgnoreProperties("parent")
@Accessors(chain = true)
public class JavabeanProperty {

    private JavabeanProperty parent;

    private String path;

    private String name;

    private String description;

    private JsonTypeEnum jsonType;

    private String jsonFormat;

    private String rawJsonSchema;

    private Boolean nullable;

    private Collection<JavabeanPropertyValidator> validators;

    private Collection<JavabeanProperty> children;

    @Override
    public String toString() {
        return JsonUtils.beautify(this);
    }

}

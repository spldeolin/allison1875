package com.spldeolin.allison1875.da.approved.javabean;

import java.util.Collection;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.spldeolin.allison1875.da.approved.enums.JsonTypeEnum;
import lombok.Data;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * @author Deolin 2020-04-25
 */
@Data
@JsonInclude(Include.NON_NULL)
@ToString(exclude = {"parent"}) // StackOverflowError
@Accessors(chain = true)
public class JavabeanProperty {

    private JavabeanProperty parent;

    private String path;

    private String name;

    private String description;

    private JsonTypeEnum jsonType;

    private String jsonFormat;

    private JsonSchema rawJsonSchema;

    private Boolean nullable;

    private Collection<JavabeanPropertyValidator> validators;

    private Collection<JavabeanProperty> children;

}

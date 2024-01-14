package com.spldeolin.allison1875.docanalyzer.javabean;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.google.common.collect.Lists;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

/**
 * @author Deolin 2020-04-27
 */
@JsonInclude(Include.NON_NULL)
@Data
@Accessors(chain = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class JsonPropertyDescriptionValueDto {

    public static final JsonPropertyDescriptionValueDto EMPTY =
            new JsonPropertyDescriptionValueDto().setDescriptionLines(
            Lists.newArrayList()).setJsonFormatPattern("");

    String annotatedName;

    List<String> descriptionLines;

    /**
     * 解析自声明在Field上的校验注解
     *
     * e.g: @NotEmpty private List<String> userNames;
     */
    List<ValidatorDto> valids = Lists.newArrayList();

    String jsonFormatPattern;

    Boolean isFieldCrossingValids = false;

    Boolean docIgnore = false;

    String referencePath;

    List<EnumCodeAndTitleDto> ecats;

    /**
     * 拓展信息
     */
    Object more;

}
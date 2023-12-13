package com.spldeolin.allison1875.docanalyzer.javabean;

import java.util.Collection;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.google.common.collect.Lists;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author Deolin 2020-04-27
 */
@JsonInclude(Include.NON_NULL)
@Data
@Accessors(chain = true)
public class JsonPropertyDescriptionValueDto {

    public static final JsonPropertyDescriptionValueDto EMPTY =
            new JsonPropertyDescriptionValueDto().setDescriptionLines(
            Lists.newArrayList()).setJsonFormatPattern("");

    private String annotatedName;

    private Collection<String> descriptionLines;

    /**
     * 解析自声明在Field上的校验注解
     *
     * e.g: @NotEmpty private Collection<String> userNames;
     */
    private List<ValidatorDto> valids = Lists.newArrayList();

    private String jsonFormatPattern;

    private Boolean isFieldCrossingValids = false;

    private Boolean docIgnore = false;

    private String referencePath;

    private Collection<EnumCodeAndTitleDto> ecats;

    /**
     * 拓展信息
     */
    private Object more;

}
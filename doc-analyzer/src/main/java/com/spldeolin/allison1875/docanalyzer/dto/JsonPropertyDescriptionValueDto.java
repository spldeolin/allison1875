package com.spldeolin.allison1875.docanalyzer.dto;

import java.util.Collection;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author Deolin 2020-04-27
 */
@Data
@Accessors(chain = true)
@JsonInclude(Include.NON_NULL)
public class JsonPropertyDescriptionValueDto {

    private String description;

    /**
     * 解析自声明在Field上的校验注解
     *
     * e.g: @NotEmpty private Collection<String> userNames;
     */
    private Collection<ValidatorDto> validators;

    private String jsonFormatPattern;

    /**
     * 解析自Field类型的唯一一个泛型上的校验注解（如果有唯一泛型的话）
     *
     * e.g: private Collection<@NotBlank @Length(max = 10) String> userNames;
     */
    private Collection<ValidatorDto> theOnlyTypeArgumentValidators;

}
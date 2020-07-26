package com.spldeolin.allison1875.docanalyzer.dto;

import java.util.Collection;
import org.apache.commons.collections4.CollectionUtils;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.spldeolin.allison1875.base.util.StringUtils;
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

    public String toString() {
        StringBuilder sb = new StringBuilder(64);
        if (StringUtils.isNotBlank(description)) {
            sb.append(description).append("\n");
        }

        if (CollectionUtils.isNotEmpty(validators)) {
            for (ValidatorDto validator : validators) {
                sb.append(validator.getValidatorType()).append(validator.getNote()).append("，");
            }
            sb.deleteCharAt(sb.lastIndexOf("，"));
            sb.append("\n");
        }

        if (StringUtils.isNotBlank(jsonFormatPattern)) {
            sb.append(jsonFormatPattern).append("\n");
        }

        return sb.toString();
    }

}
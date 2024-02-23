package com.spldeolin.allison1875.docanalyzer.javabean;

import java.util.List;
import com.github.javaparser.utils.StringEscapeUtils;
import com.google.common.collect.Lists;
import com.spldeolin.allison1875.common.util.JsonUtils;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Deolin 2020-04-27
 */
@Data
@Accessors(chain = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@Slf4j
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

    List<AnalyzeEnumConstantRetval> ecats;

    /**
     * 拓展信息
     */
    Object moreInfo;

    public String serialize() {
        return JsonUtils.toJson(this);
    }

    public static JsonPropertyDescriptionValueDto deserialize(String json) {
        if (json == null) {
            return null;
        }
        try {
            return JsonUtils.toObject(json, JsonPropertyDescriptionValueDto.class);
        } catch (Exception e) {
            log.info("jpdv has been pretty [{}]", StringEscapeUtils.escapeJava(json));
            return null;
        }

    }

}